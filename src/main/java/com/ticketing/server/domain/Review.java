package com.ticketing.server.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reviews",
        uniqueConstraints = {
                // 🌟 한 유저는 한 공연에 1개만 작성 가능
                @UniqueConstraint(
                        name = "uk_review_user_event",
                        columnNames = {"user_id", "event_id"}
                )
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // 1~5 별점
    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Review(User user, Event event, int rating, String content) {
        validateRating(rating);
        validateContent(content);
        this.user = user;
        this.event = event;
        this.rating = rating;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // 🌟 본인 수정 메서드 (Dirty Checking)
    public void update(int rating, String content) {
        validateRating(rating);
        validateContent(content);
        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("후기 내용은 비어있을 수 없습니다.");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("후기는 1000자 이내로 작성해주세요.");
        }
    }
}
