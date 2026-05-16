package com.ticketing.server.config;

import com.ticketing.server.domain.User;
import com.ticketing.server.domain.UserRole;
import com.ticketing.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 서버 시작 시 관리자 계정이 없으면 1개 자동 생성합니다.
 *
 * 비밀번호는 application.yml의 admin.password 값을 사용하고,
 * 값이 없으면 안전한 기본값 대신 생성을 건너뜁니다.
 *
 * application.yml 예시:
 *   admin:
 *     email: admin@dearticket.com
 *     name: 관리자
 *     password: ${ADMIN_PASSWORD:changeme1234!}
 */
@Slf4j
@Component
@Order(2)  // CategoryDataLoader(Order=1) 다음
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@dearticket.com}")
    private String adminEmail;

    @Value("${admin.name:관리자}")
    private String adminName;

    @Value("${admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        // 1) 이미 존재하면 스킵
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("✅ [ADMIN_INIT] 관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // 2) 비밀번호 미설정 시 생성 건너뜀
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("⚠️ [ADMIN_INIT] admin.password 가 설정되지 않아 관리자 계정 생성을 건너뜁니다. " +
                     "application.yml 또는 환경변수 ADMIN_PASSWORD 를 설정하세요.");
            return;
        }

        // 3) 계정 생성
        User admin = User.builder()
                .email(adminEmail)
                .name(adminName)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ROLE_ADMIN)
                .point(0L)
                .build();

        userRepository.save(admin);

        log.info("✨ [ADMIN_INIT] 관리자 계정이 생성되었습니다!");
        log.info("   📧 Email: {}", adminEmail);
        log.info("   👤 Name : {}", adminName);
        log.info("   🔑 Password 는 application.yml/환경변수에서 관리해주세요.");
    }
}
