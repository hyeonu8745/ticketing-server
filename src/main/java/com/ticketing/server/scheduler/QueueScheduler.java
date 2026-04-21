package com.ticketing.server.scheduler;

import com.ticketing.server.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;
    private final RedissonClient redissonClient; // 키 검색을 위해 추가

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        // 수정된 부분: Set 대신 Iterable을 사용하고, getKeysByPattern을 호출합니다.
        Iterable<String> waitingKeys = redissonClient.getKeys().getKeysByPattern("queue:waiting:event:*");

        int enterCount = 100; // 한 번에 입장시킬 인원

        // Iterable도 Set처럼 for-each 문을 똑같이 사용할 수 있습니다.
        for (String key : waitingKeys) {
            try {
                String[] parts = key.split(":");
                Long eventId = Long.parseLong(parts[parts.length - 1]);

                queueService.letUsersEnter(eventId, enterCount);
            } catch (Exception e) {
                log.error("[SCHEDULER_ERROR] 키 {} 처리 중 오류 발생: {}", key, e.getMessage());
            }
        }
    }
}