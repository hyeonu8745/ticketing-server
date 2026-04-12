package com.ticketing.server;

import com.ticketing.server.domain.User;
// import com.ticketing.server.domain.UserRole; // 현우 님의 Enum 클래스명에 맞게 주석 해제하세요
import com.ticketing.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class UserDataInitTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 🌟 테스트 실행 전, 이전 테스트로 인해 꼬이지 않도록 유저를 싹 비워줍니다.
        // (단, 이 테스트는 로컬 DB를 날릴 수 있으니 주의하세요!)
        // userRepository.deleteAll();
    }

    @Test
    @DisplayName("테스트용 유저 1000명 대량 생성")
    void insert1000Users() {
        // 🌟 방어 로직: 이미 데이터가 1000개 이상 있다면 중복 에러 방지를 위해 실행하지 않음
        if (userRepository.count() >= 1000) {
            System.out.println("✅ 이미 1000명 이상의 유저가 존재하여 생성을 건너뜁니다.");
            return;
        }

        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode("1234"); // 비밀번호 암호화는 밖에서 한 번만 (성능 최적화)

        for (int i = 1; i <= 1000; i++) {
            User user = User.builder()
                    .email("user" + i + "@test.com") // user1@test.com ~ user1000@test.com
                    .name("테스트유저" + i)
                    .password(encodedPassword)
                    .point(100000L) // 기본 포인트 10만 지급
                    // .role(UserRole.USER) // 🌟 현우님의 권한 Enum 설정에 맞춰 주석 해제
                    .build();
            users.add(user);
        }

        // 🌟 한 건씩 save() 하지 않고, 리스트에 모아서 saveAll()로 한 방에 DB에 꽂아 넣습니다.
        userRepository.saveAll(users);

        System.out.println("🚀 1000명 유저 생성 완료!");
    }
}