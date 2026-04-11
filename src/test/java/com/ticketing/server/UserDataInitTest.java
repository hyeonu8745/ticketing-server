package com.ticketing.server; // 현우 님의 실제 패키지명으로 수정하세요!

import com.ticketing.server.domain.User;
import com.ticketing.server.repository.UserRepository;
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

    @Test
    void insertTestUsers() {
        List<User> users = new ArrayList<>();
        // 비밀번호는 모두 동일하게 세팅 (BCrypt 암호화)
        String encodedPassword = passwordEncoder.encode("password123!");

        for (int i = 1; i <= 1000; i++) {
            User user = User.builder()
                    .email("test" + i + "@test.com")
                    .password(encodedPassword)
                    .name("테스트유저" + i)
                    .build();
            users.add(user);
        }

        // 1,000명을 한 번에 DB에 꽂아넣기!
        userRepository.saveAll(users);
        System.out.println("✅ 1,000명의 테스트 유저가 성공적으로 생성되었습니다!");
    }
}