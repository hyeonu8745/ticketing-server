package com.ticketing.server;

import com.ticketing.server.domain.User;
import com.ticketing.server.domain.UserRole;
import com.ticketing.server.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals; // 🌟 Assertion 추가

@SpringBootTest
class UserDataInitTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("JMeter 테스트용 유저 추가 생성 (1001~10000)")
    @Transactional
    @Rollback(false)
    void insertRemainingUsers() {
        int startNum = 1001;  // 🌟 시작 번호 설정
        int targetNum = 10000; // 최종 목표 번호

        // 현재 DB에 있는 유저 수 확인
        long currentCount = userRepository.count();
        System.out.println("📊 현재 DB 유저 수: " + currentCount);

        if (currentCount >= targetNum) {
            System.out.println("✅ 이미 10,000명 이상의 유저가 존재합니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode("1234"); // 암호화는 루프 밖에서 1번만
        List<User> userList = new ArrayList<>();

        System.out.println("⏳ " + startNum + "번부터 유저 생성 시작...");

        for (int i = startNum; i <= targetNum; i++) {
            userList.add(User.builder()
                    .email("user" + i + "@test.com")
                    .name("테스트유저" + i)
                    .password(encodedPassword)
                    .point(100000L) // 예매 테스트용 포인트 지급
                    .role(UserRole.ROLE_USER)
                    .build());

            // 1,000명 단위로 끊어서 저장하여 트랜잭션 및 메모리 효율화
            if (i % 1000 == 0) {
                userRepository.saveAll(userList);
                userRepository.flush(); // DB에 즉시 반영
                userList.clear(); // 리스트 비우기 (메모리 관리)
                System.out.println("... " + i + "명까지 생성 완료");
            }
        }

        // 남은 인원(9001~10000 이후 등) 저장
        if (!userList.isEmpty()) {
            userRepository.saveAll(userList);
        }

        // 최종 결과 검증
        long finalCount = userRepository.count();
        System.out.println("🚀 최종 생성 완료! 현재 총 유저 수: " + finalCount);
        assertEquals(targetNum, (int)finalCount, "총 유저 수가 10,000명이어야 합니다.");
    }
}