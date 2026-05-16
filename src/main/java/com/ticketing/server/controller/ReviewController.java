package com.ticketing.server.controller;

import com.ticketing.server.dto.*;
import com.ticketing.server.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ──────────────────────────────────────────────
    // 1. 공연별 후기 목록 조회 (공개)
    //    GET /api/reviews/events/{eventId}
    // ──────────────────────────────────────────────
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(reviewService.getReviewsByEvent(eventId));
    }

    // ──────────────────────────────────────────────
    // 2. 공연별 평점 요약 (공개)
    //    GET /api/reviews/events/{eventId}/summary
    // ──────────────────────────────────────────────
    @GetMapping("/events/{eventId}/summary")
    public ResponseEntity<ReviewSummaryResponse> getSummary(@PathVariable Long eventId) {
        return ResponseEntity.ok(reviewService.getSummary(eventId));
    }

    // ──────────────────────────────────────────────
    // 3. 작성 가능 여부 체크 (인증 필요)
    //    GET /api/reviews/events/{eventId}/eligibility
    // ──────────────────────────────────────────────
    @GetMapping("/events/{eventId}/eligibility")
    public ResponseEntity<?> checkEligibility(
            @PathVariable Long eventId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reviewService.checkEligibility(eventId, userId));
    }

    // ──────────────────────────────────────────────
    // 4. 후기 작성 (인증 필요)
    //    POST /api/reviews/events/{eventId}
    // ──────────────────────────────────────────────
    @PostMapping("/events/{eventId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long eventId,
            @RequestBody ReviewRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = (Long) authentication.getPrincipal();

        try {
            ReviewResponse created = reviewService.createReview(eventId, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 5. 후기 수정 (인증 + 본인만)
    //    PUT /api/reviews/{reviewId}
    // ──────────────────────────────────────────────
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = (Long) authentication.getPrincipal();

        try {
            return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 6. 후기 삭제 (인증 + 본인만)
    //    DELETE /api/reviews/{reviewId}
    // ──────────────────────────────────────────────
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = (Long) authentication.getPrincipal();

        try {
            reviewService.deleteReview(reviewId, userId);
            return ResponseEntity.ok("후기가 삭제되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 7. 내 후기 목록 (인증)
    //    GET /api/reviews/me
    // ──────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reviewService.getMyReviews(userId));
    }
}
