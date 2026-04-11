package com.ticketing.server.scheduler;

import com.ticketing.server.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;

    /**
     * 1초마다 대기열 상위 N명을 활성 상태(Active)로 전환합니다.
     * fixedDelay = 1000은 작업이 끝난 후 1초 뒤에 다시 실행한다는 뜻입니다.
     */
    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        // 실제 운영 시에는 DB에서 현재 예매 진행 중인 이벤트 ID 리스트를 가져와서 반복문을 돌려야 하지만,
        // 지금은 테스트를 위해 1번 이벤트(eventId = 1L)를 기준으로 고정해서 작성합니다.
        Long eventId = 1L;

        // 한 번에 몇 명씩 입장시킬지 결정 (졸업 작품 발표 때 이 숫자를 바꿔가며 성능을 보여주기 좋습니다!)
        int enterCount = 10;

        try {
            queueService.letUsersEnter(eventId, enterCount);
            // log.info("[SCHEDULER] 이벤트 {}의 대기열을 처리했습니다. ({}명 입장)", eventId, enterCount);
        } catch (Exception e) {
            log.error("[SCHEDULER_ERROR] 대기열 처리 중 오류 발생: {}", e.getMessage());
        }
    }
}