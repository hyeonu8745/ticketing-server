package com.ticketing.server.service;

import com.ticketing.server.dto.QueueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final RedissonClient redissonClient;

    // Redis에 저장될 키 이름 (공연별로 줄을 따로 섭니다!)
    private static final String WAITING_QUEUE_KEY = "queue:waiting:event:";
    private static final String ACTIVE_QUEUE_KEY = "queue:active:event:";

    public QueueResponse joinQueue(Long eventId, Long userId) {
        RScoredSortedSet<Long> waitingQueue = redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + eventId);
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);

        // 1. 이미 ACTIVE(입장 성공) 상태라면 바로 통과
        if (activeQueue.contains(userId)) {
            return new QueueResponse("ACTIVE", 0L);
        }

        // 🌟 2. 하이패스 로직 추가: ACTIVE 인원이 1,000명 미만이면 즉시 입장
        // size()가 1000보다 작으면 대기열을 거치지 않고 바로 ACTIVE에 넣어줍니다.
        if (activeQueue.size() < 1000) {
            activeQueue.add(System.currentTimeMillis(), userId);
            log.info("🚀 [하이패스] 유저 {} 즉시 입장 (현재 입장 인원: {}명)", userId, activeQueue.size());
            return new QueueResponse("ACTIVE", 0L);
        }

        // 3. 1,000명이 꽉 찼다면? 그때부터 대기열(Waiting)에 줄을 세움
        if (!waitingQueue.contains(userId)) {
            waitingQueue.add(System.currentTimeMillis(), userId);
            log.info("⏳ [대기열 발생] 유저 {} 대기열 진입 (공연 {})", userId, eventId);
        }

        Integer rank = waitingQueue.rank(userId);
        Long position = (rank != null) ? rank.longValue() : 0L;

        return new QueueResponse("WAITING", position);
    }
    // QueueService.java 에 추가할 메서드
    public void letUsersEnter(Long eventId, int count) {
        RScoredSortedSet<Long> waitingQueue = redissonClient.getScoredSortedSet(WAITING_QUEUE_KEY + eventId);
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);

        // 대기열에서 가장 오래 기다린(점수가 가장 낮은) 0번부터 count-1번까지의 유저를 가져옵니다.
        var usersToEnter = waitingQueue.valueRange(0, count - 1);

        for (Long userId : usersToEnter) {
            // 입장 줄(Active)로 이동시키고
            activeQueue.add(System.currentTimeMillis(), userId);
            // 대기 줄(Waiting)에서는 빼줍니다.
            waitingQueue.remove(userId);

            log.info("🎉 유저 {} 예매창 입장! (공연 {})", userId, eventId);
        }
    }
    // QueueService.java에 추가
    public boolean isAllowedToReserve(Long eventId, Long userId) {
        RScoredSortedSet<Long> activeQueue = redissonClient.getScoredSortedSet(ACTIVE_QUEUE_KEY + eventId);
        // 예매창(Active)에 들어온 사람만 true 반환!
        return activeQueue.contains(userId);
    }
}