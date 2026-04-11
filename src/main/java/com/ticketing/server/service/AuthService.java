package com.ticketing.server.service;

import com.ticketing.server.domain.User;
import com.ticketing.server.domain.UserRole;
import com.ticketing.server.dto.AuthResponse;
import com.ticketing.server.dto.LoginRequest;
import com.ticketing.server.dto.SignupRequest;
import com.ticketing.server.repository.UserRepository;
import com.ticketing.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 만든 암호화 도구
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 회원가입 로직
    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화 후 User 엔티티 생성 및 저장
        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password())) // 무조건 암호화 필수!
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    // 2. 로그인 로직 (토큰 발급)
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 유저 찾기
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호까지 맞았다면 토큰 발급!
        String token = jwtTokenProvider.createToken(user.getId(), user.getRole().name());

        return new AuthResponse(token);
    }
}