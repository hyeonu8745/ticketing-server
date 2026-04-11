package com.ticketing.server.repository;

import com.ticketing.server.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 나중에 로그인(JWT 발급)할 때 이메일로 유저를 찾기 위해 필수적인 메서드입니다.
    Optional<User> findByEmail(String email);

    // 회원가입 시 이메일 중복 체크를 위한 메서드
    boolean existsByEmail(String email);
}