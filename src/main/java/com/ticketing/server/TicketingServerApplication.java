package com.ticketing.server;

import com.ticketing.server.domain.*;
import com.ticketing.server.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class TicketingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            SeatRepository seatRepository,    // 주입 확인!
            EventRepository eventRepository,  // 주입 확인!
            PasswordEncoder passwordEncoder) {
        return args -> {

            // 1. 유저 생성 로직 (유저가 부족할 때만 실행)
            long userCount = userRepository.count();
            if (userCount < 1000) {
                System.out.println("🚀 유저가 부족하여 1,000명 생성을 시작합니다...");
                List<User> users = new ArrayList<>();
                String pw = passwordEncoder.encode("password123!");
                for (int i = 1; i <= 1000; i++) {
                    users.add(User.builder()
                            .email("test" + i + "@test.com")
                            .password(pw)
                            .name("테스트유저" + i)
                            .point(1000000L) // 포인트도 넉넉히!
                            .build());
                }
                userRepository.saveAll(users);
                System.out.println("✅ 유저 1,000명 생성 완료!");
            } else {
                System.out.println("ℹ️ 유저가 이미 존재합니다. (현재: " + userCount + "명)");
            }

            // 2. 좌석 생성 로직 (유저 상태와 상관없이 '좌석'만 없으면 실행)
            // 🌟 이 블록을 밖으로 뺀 게 핵심입니다!
            if (eventRepository.count() == 0) {
                System.out.println("🎟️ 좌석 데이터가 없어 생성을 시작합니다...");

                Event event = Event.builder()
                        .title("2026 졸업 작품 콘서트")
                        .build();
                eventRepository.save(event);

                List<Seat> seats = new ArrayList<>();
                for (int i = 1; i <= 100; i++) {
                    seats.add(Seat.builder()
                            .event(event)
                            .seatNumber("A-" + i)
                            .status(SeatStatus.AVAILABLE)
                            .price(50000L) // 5만원 세팅
                            .build());
                }
                seatRepository.saveAll(seats);
                System.out.println("✅ 좌석 100개 생성 완료! (80번 좌석 준비됨)");
            } else {
                System.out.println("ℹ️ 이미 이벤트와 좌석이 존재합니다.");
            }
        };
    }
}