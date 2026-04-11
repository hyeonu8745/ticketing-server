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
    private final ReservationService reservationService; // 기존 서비스 주입

    public void reserveSeatWithLock(Long eventId, Long seatId, Long userId) {
        String lockKey = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(10, 2, TimeUnit.SECONDS)) {
                try {
                    // 이제 여기서 eventId를 사용할 수 있습니다!
                    reservationService.reserveSeat(eventId, seatId, userId);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("락 획득 실패");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}