package com.ticketing.server.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final RedissonClient redissonClient;
    private final ReservationService reservationService;
    private final QueueService queueService; // 🌟 추가

    public void reserveSeatWithLock(Long eventId, Long seatId, Long userId) {
        String lockKey = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 대기 시간을 10초로 넉넉히 주셨으니, 1만 명 테스트에 적합합니다.
            if (lock.tryLock(10, 2, TimeUnit.SECONDS)) {
                try {
                    // 🌟 [추가] 락 획득 성공 후, 진짜 입장권(ACTIVE)이 유효한지 최종 확인
                    if (!queueService.isAllowedToReserve(eventId, userId)) {
                        throw new RuntimeException("정상적인 대기열 진입이 확인되지 않았습니다.");
                    }

                    reservationService.reserveSeat(eventId, seatId, userId);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("이미 다른 분이 예매 중인 좌석입니다. (락 획득 실패)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("예매 중 통신 오류가 발생했습니다.");
        }
    }
}