package com.ticketing.server.service;

import com.ticketing.server.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final RedissonClient redissonClient;

    private static final String WAITING_QUEUE_KEY = "queue:waiting:event:";
    private static final String ACTIVE_QUEUE_KEY = "queue:active:event:";

    public QueueResponse joinQueue(Long eventId, Long userId) {
        RScoredSortedSet<Long> waitingQueue = redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + eventId);
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);

        // 1. 이미 입장(ACTIVE) 상태인지 확인
        if (activeQueue.contains(userId)) {
            return new QueueResponse("ACTIVE", 0L);
        }

        // 2. 하이패스: 활성 인원이 500명 미만이면 즉시 입장 (부하 분산 위해 인원 조정 가능)
        if (activeQueue.size() < 500) {
            activeQueue.add(System.currentTimeMillis(), userId);
            log.info("🚀 [하이패스] 유저 {} 즉시 입장 (공연 {})", userId, eventId);
            return new QueueResponse("ACTIVE", 0L);
        }

        // 3. 대기열 진입 (이미 있다면 기존 순번 반환)
        Double score = waitingQueue.getScore(userId);
        if (score == null) {
            waitingQueue.add(System.currentTimeMillis(), userId);
            log.info("⏳ [대기열 진입] 유저 {} (공연 {})", userId, eventId);
        }

        Integer rank = waitingQueue.rank(userId);
        return new QueueResponse("WAITING", (rank != null) ? rank.longValue() + 1 : 1L);
    }

    public void letUsersEnter(Long eventId, int count) {
        RScoredSortedSet<Long> waitingQueue = redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + eventId);
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);

        // 대기열 상위 유저들 가져오기
        Collection<Long> usersToEnter = waitingQueue.valueRange(0, count - 1);

        if (usersToEnter.isEmpty()) return;

        for (Long userId : usersToEnter) {
            // Active로 이동 후 Waiting에서 제거
            activeQueue.add(System.currentTimeMillis(), userId);
            waitingQueue.remove(userId);
            log.info("🎉 [입장 승인] 유저 {} -> 공연 {} 예매 시작!", userId, eventId);
        }
    }

    public boolean isAllowedToReserve(Long eventId, Long userId) {
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);
        return activeQueue.contains(userId);
    }

    public void exitActiveQueue(Long eventId, Long userId) {
        String key = "queue:active:event:" + eventId; // 활성 큐 키 (사용 중인 이름과 맞추세요)
        redissonClient.getScoredSortedSet(key).remove(userId);
        log.info("🚪 [QUEUE_EXIT] 유저 {} 님 대기열 퇴장 처리", userId);
    }
}