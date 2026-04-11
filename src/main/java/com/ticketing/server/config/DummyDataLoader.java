package com.ticketing.server.config;

import com.ticketing.server.domain.Event;
import com.ticketing.server.domain.Seat;
import com.ticketing.server.domain.SeatStatus;
import com.ticketing.server.repository.EventRepository;
import com.ticketing.server.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DummyDataLoader implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있다면 중복 생성하지 않고 패스!
        if (eventRepository.count() > 0) {
            return;
        }

        // 1. 현우님의 Event 엔티티에 맞춰서 1번 공연 생성!
        Event event = Event.builder()
                .title("현우의 대규모 티켓팅 콘서트")
                .startTime(LocalDateTime.now().plusDays(7)) // 공연 시간은 일주일 뒤로 세팅
                .totalSeats(100)
                .build();
        eventRepository.save(event);

        // 2. 1번 공연에 속한 좌석 100개 생성 (seatId 1 ~ 100)
        for (int i = 1; i <= 100; i++) {
            Seat seat = Seat.builder()
                    .event(event)
                    .seatNumber("A-" + i)
                    .status(SeatStatus.AVAILABLE) // 모두 예매 가능 상태로 초기화
                    .build();
            seatRepository.save(seat);
        }

        System.out.println("✅ 더미 데이터(공연 1개, 좌석 100개) 자동 세팅 완료!");
    }
}