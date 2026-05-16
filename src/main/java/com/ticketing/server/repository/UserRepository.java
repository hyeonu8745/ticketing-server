package com.ticketing.server.repository;

import com.ticketing.server.domain.User;
import com.ticketing.server.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 🌟 관리자용: 이메일/이름 통합 검색 (페이징)
    @Query("""
        SELECT u FROM User u
        WHERE :keyword IS NULL OR :keyword = ''
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<User> findAllForAdmin(@Param("keyword") String keyword, Pageable pageable);

    long countByRole(UserRole role);
}
