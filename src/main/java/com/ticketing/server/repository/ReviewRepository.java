package com.ticketing.server.repository;

import com.ticketing.server.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 공연별 후기 목록 (최신순)
    List<Review> findAllByEventIdOrderByCreatedAtDesc(Long eventId);

    // 내 후기 목록 (최신순)
    List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 유저가 특정 공연에 이미 작성했는지
    Optional<Review> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // 🌟 공연별 평균 평점 (소수 첫째자리)
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.event.id = :eventId")
    Double findAverageRatingByEventId(Long eventId);

    long countByEventId(Long eventId);
}
