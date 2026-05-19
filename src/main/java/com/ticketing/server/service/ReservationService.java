package com.ticketing.server.service;

import com.ticketing.server.domain.*;
import com.ticketing.server.dto.ReservationResponse;
import com.ticketing.server.repository.PaymentRepository;
import com.ticketing.server.repository.ReservationRepository;
import com.ticketing.server.repository.SeatRepository;
import com.ticketing.server.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final RedissonClient redissonClient;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final QueueService queueService;

    /**
     * 좌석 예매 핵심 로직.
     * 분산 락은 ReservationFacade에서 처리하므로 이 메서드는 락이 이미 획득된 상태에서 호출된다.
     */
    @Transactional
    public void reserveSeat(Long eventId, Long seatId, Long userId) {

        // 대기열 검문 (입구 컷)
        if (!queueService.isAllowedToReserve(eventId, userId)) {
            log.warn("[RESERVE_REJECTED] 대기열 미통과 유저. UserId: {}, EventId: {}", userId, eventId);
            throw new RuntimeException("아직 예매 순서가 아닙니다. 대기열을 확인해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));

        if (seat.getStatus() == SeatStatus.RESERVED) {
            throw new RuntimeException("이미 예약된 좌석입니다.");
        }

        // 포인트 결제 및 좌석 예약
        user.usePoint(seat.getPrice());
        seat.reserve();

        // 예약 데이터 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)
                .reservedAt(LocalDateTime.now())
                .build();
        reservationRepository.save(reservation);

        // 결제 이력(Payment) 생성 및 저장
        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(seat.getPrice())
                .status(PaymentStatus.COMPLETED)
                .build();
        paymentRepository.save(payment);

        // 예약 성공 후 대기열 퇴장 처리
        queueService.exitActiveQueue(eventId, userId);

        log.info("🎉 [RESERVE_SUCCESS] 유저 {}님 예약 완료! (대기열 종료)", userId);
    }

    /**
     * 예약 취소 및 환불
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        // 1. 시작 로그 (과정 추적용)
        log.info("[CANCEL_ATTEMPT] ReservationId: {}, UserId: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("[CANCEL_FAIL] 예약 내역 없음. ResId: {}", reservationId);
                    return new RuntimeException("예약 내역을 찾을 수 없습니다.");
                });

        if (!reservation.getUser().getId().equals(userId)) {
            log.error("[CANCEL_FAIL] 권한 없음. UserId: {}, OwnerId: {}", userId, reservation.getUser().getId());
            throw new RuntimeException("취소 권한이 없습니다.");
        }

        Seat seat = reservation.getSeat();
        User user = reservation.getUser();

        // 환불 및 상태 변경 로직...
        user.chargePoint(seat.getPrice());
        seat.cancel();
        reservation.cancel(); // 🌟 소프트 딜리트

        // 2. 최종 성공 로그 (결과 확인용)
        log.info("[CANCEL_SUCCESS] UserId: {}, ResId: {}, Refund: {}원",
                userId, reservationId, seat.getPrice());
    }

    /**
     * 좌석 변경 (고급 기능: 기존 좌석 취소 + 새 좌석 예약)
     * 졸업 작품에서 "동시성 제어를 고려한 변경"으로 발표하기 좋습니다.
     */
    @Transactional
    public void changeSeat(Long reservationId, Long newSeatId, Long userId) {
        // 새 좌석에 대한 락 획득 시도
        String lockKey = "lock:seat:" + newSeatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 2, TimeUnit.SECONDS)) {
                try {
                    Reservation reservation = reservationRepository.findById(reservationId)
                            .orElseThrow(() -> new RuntimeException("기존 예약 정보를 찾을 수 없습니다."));

                    Seat oldSeat = reservation.getSeat();
                    Seat newSeat = seatRepository.findById(newSeatId)
                            .orElseThrow(() -> new RuntimeException("새 좌석을 찾을 수 없습니다."));

                    if (newSeat.getStatus() == SeatStatus.RESERVED) {
                        throw new RuntimeException("새 좌석이 이미 예약되었습니다.");
                    }

                    // 차액 계산 (가격이 다를 경우 대비)
                    long priceDiff = newSeat.getPrice() - oldSeat.getPrice();
                    User user = reservation.getUser();

                    if (user.getPoint() < priceDiff) {
                        throw new RuntimeException("잔액이 부족하여 변경할 수 없습니다.");
                    }

                    // 로직 실행: 기존 좌석 풀고 -> 새 좌석 잡고 -> 차액 결제
                    oldSeat.cancel();
                    newSeat.reserve();
                    user.usePoint(priceDiff);

                    // 예약 정보 업데이트
                    reservation.updateSeat(newSeat); // Reservation 엔티티에 메서드 필요

                    log.info("🔄 유저 {}님 좌석 변경 성공! (A-{} -> A-{})",
                            userId, oldSeat.getSeatNumber(), newSeat.getSeatNumber());

                } finally {
                    if (lock.isHeldByCurrentThread()) lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 마이페이지: 내 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        // 🌟 메서드 이름을 레포지토리와 맞추고, CONFIRMED 상태를 파라미터로 넣어줍니다.
        return reservationRepository.findAllByUserIdAndStatusOrderByReservedAtDesc(userId, ReservationStatus.CONFIRMED)
                .stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getSeat().getEvent().getId(),
                        reservation.getSeat().getEvent().getTitle(),
                        reservation.getSeat().getSeatNumber(),
                        reservation.getSeat().getPrice(),
                        reservation.getReservedAt()
                ))
                .toList();
    }

    /**
     * 봇 탐지용 — 유저의 취소 횟수 조회
     */
    @Transactional(readOnly = true)
    public int countCancelledByUser(Long userId) {
        return reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CANCELLED);
    }

    /**
     * 봇 탐지용 — 유저의 성공 예매 횟수 조회
     */
    @Transactional(readOnly = true)
    public int countConfirmedByUser(Long userId) {
        return reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CONFIRMED);
    }

    // 🌟 서킷 브레이커 적용! 이름은 yml에서 정한 ticketingService
    // fallbackMethod: 에러가 나거나 서킷이 열렸을 때 대신 실행될 메서드
    @CircuitBreaker(name = "ticketingService", fallbackMethod = "fallbackReserve")
    public String reserveTicket(Long eventId, Long userId) {
        // 원래 실행될 예약 로직 (예: DB 저장, Redis 호출 등)
        // 만약 여기서 에러가 나거나 시간이 너무 오래 걸리면 서킷이 감지합니다.
        return "예약이 완료되었습니다.";
    }

    // 🛡️ 서킷이 열렸을 때 사용자가 볼 "안내 문구"
    public String fallbackReserve(Long eventId, Long userId, Throwable t) {
        // 로그를 남겨서 원인을 파악합니다.
        // t는 어떤 에러 때문에 서킷이 작동했는지 알려줍니다.
        return "현재 접속자가 너무 많아 시스템이 잠시 휴식 중입니다. 10초 뒤에 다시 시도해주세요!";
    }
}