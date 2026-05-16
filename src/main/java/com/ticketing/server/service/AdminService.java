package com.ticketing.server.service;

import com.ticketing.server.domain.*;
import com.ticketing.server.dto.admin.*;
import com.ticketing.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationService reservationService;  // 강제 취소 위임

    // ════════════════════════════════════════════════════════════════
    // 📊 대시보드 통계
    // ════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long visibleEvents = eventRepository.countByVisibleTrue();
        long totalReservations = reservationRepository.count();
        long activeReservations = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long totalReviews = reviewRepository.count();
        long totalRevenue = reservationRepository.sumPriceByStatus(ReservationStatus.CONFIRMED);

        // 카테고리별
        List<AdminDashboardResponse.CategoryCount> byCategory = reservationRepository.countEventsByCategory().stream()
                .map(arr -> AdminDashboardResponse.CategoryCount.builder()
                        .category((String) arr[0])
                        .count(((Number) arr[1]).longValue())
                        .build())
                .toList();

        // 최근 7일 추이 (값 없는 날은 0으로 채움)
        LocalDateTime since = LocalDate.now().minusDays(6).atStartOfDay();
        Map<String, Long> dailyMap = reservationRepository.findDailyCount(since).stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> ((Number) arr[1]).longValue(),
                        (a, b) -> a
                ));
        List<AdminDashboardResponse.DailyCount> daily7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String key = d.toString();
            daily7.add(AdminDashboardResponse.DailyCount.builder()
                    .date(key)
                    .count(dailyMap.getOrDefault(key, 0L))
                    .build());
        }

        // TOP 5
        List<AdminDashboardResponse.TopEvent> topEvents = reservationRepository
                .findTopEventsByReservation(PageRequest.of(0, 5))
                .stream()
                .map(arr -> AdminDashboardResponse.TopEvent.builder()
                        .eventId(((Number) arr[0]).longValue())
                        .title((String) arr[1])
                        .reservationCount(((Number) arr[2]).longValue())
                        .build())
                .toList();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalEvents(totalEvents)
                .visibleEvents(visibleEvents)
                .totalReservations(totalReservations)
                .activeReservations(activeReservations)
                .totalReviews(totalReviews)
                .totalRevenue(totalRevenue)
                .eventsByCategory(byCategory)
                .reservationsLast7Days(daily7)
                .topEvents(topEvents)
                .build();
    }

    // ════════════════════════════════════════════════════════════════
    // 🎭 공연 관리
    // ════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Page<AdminEventResponse> getEvents(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return eventRepository.findAllForAdmin(keyword, pageable)
                .map(AdminEventResponse::from);
    }

    @Transactional
    public void hideEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));
        event.hide();
        log.info("[ADMIN] 공연 숨김 처리: eventId={}, title={}", eventId, event.getTitle());
    }

    @Transactional
    public void showEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));
        event.show();
        log.info("[ADMIN] 공연 다시 표시: eventId={}, title={}", eventId, event.getTitle());
    }

    @Transactional
    public AdminEventResponse updateEvent(Long eventId, AdminEventUpdateRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));
        event.updateInfo(req.title(), req.location(), req.description());
        log.info("[ADMIN] 공연 정보 수정: eventId={}", eventId);
        return AdminEventResponse.from(event);
    }

    // ════════════════════════════════════════════════════════════════
    // 🎫 예매 관리
    // ════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Page<AdminReservationResponse> getReservations(
            ReservationStatus status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.findAllForAdmin(status, keyword, pageable)
                .map(AdminReservationResponse::from);
    }

    /**
     * 관리자 강제 취소 — 기존 cancelReservation은 본인 검증이 있으므로
     * 권한 우회를 위해 직접 처리한다 (환불, 좌석 해제 동일)
     */
    @Transactional
    public void forceCancelReservation(Long reservationId, Long adminUserId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예매를 찾을 수 없습니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("이미 취소된 예매입니다.");
        }

        Seat seat = reservation.getSeat();
        User user = reservation.getUser();

        user.chargePoint(seat.getPrice());   // 환불
        seat.cancel();                       // 좌석 해제
        reservation.cancel();                // 상태 변경

        log.warn("[ADMIN_FORCE_CANCEL] adminId={}, resId={}, userId={}, refund={}원",
                adminUserId, reservationId, user.getId(), seat.getPrice());
    }

    // ════════════════════════════════════════════════════════════════
    // 👤 회원 관리
    // ════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return userRepository.findAllForAdmin(keyword, pageable)
                .map(u -> {
                    long count = reservationRepository.countByUserIdAndStatusEquals(
                            u.getId(), ReservationStatus.CONFIRMED);
                    return AdminUserResponse.from(u, count);
                });
    }

    @Transactional
    public AdminUserResponse changeUserRole(Long targetUserId, Long adminUserId, UserRole newRole) {
        if (targetUserId.equals(adminUserId)) {
            throw new RuntimeException("자기 자신의 권한은 변경할 수 없습니다.");
        }
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.changeRole(newRole);
        long count = reservationRepository.countByUserIdAndStatusEquals(
                user.getId(), ReservationStatus.CONFIRMED);
        log.warn("[ADMIN_ROLE_CHANGE] adminId={}, targetId={}, newRole={}",
                adminUserId, targetUserId, newRole);
        return AdminUserResponse.from(user, count);
    }

    // ════════════════════════════════════════════════════════════════
    // 💬 후기 관리
    // ════════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        // 최신순. 페이징은 일단 제외 (필요시 추가)
        return reviewRepository.findAll(Sort.by("createdAt").descending());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long adminUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));
        reviewRepository.delete(review);
        log.warn("[ADMIN_REVIEW_DELETE] adminId={}, reviewId={}", adminUserId, reviewId);
    }
}
