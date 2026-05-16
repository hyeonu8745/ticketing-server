package com.ticketing.server.service;

import com.ticketing.server.domain.*;
import com.ticketing.server.dto.*;
import com.ticketing.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // ──────────────────────────────────────────────
    // 1. 후기 작성
    // ──────────────────────────────────────────────
    @Transactional
    public ReviewResponse createReview(Long eventId, Long userId, ReviewRequest req) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 🌟 권한 체크
        verifyEligible(user, event);

        // 중복 작성 차단 (DB 유니크 제약 + 어플리케이션 가드)
        if (reviewRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new RuntimeException("이미 후기를 작성하셨습니다. 수정을 이용해주세요.");
        }

        Review review = Review.builder()
                .user(user)
                .event(event)
                .rating(req.rating())
                .content(req.content())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("[REVIEW_CREATE] userId={}, eventId={}, rating={}", userId, eventId, req.rating());
        return ReviewResponse.from(saved);
    }

    // ──────────────────────────────────────────────
    // 2. 후기 수정 (본인만)
    // ──────────────────────────────────────────────
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        review.update(req.rating(), req.content());
        log.info("[REVIEW_UPDATE] reviewId={}, userId={}", reviewId, userId);
        return ReviewResponse.from(review);
    }

    // ──────────────────────────────────────────────
    // 3. 후기 삭제 (본인만)
    // ──────────────────────────────────────────────
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("후기를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        reviewRepository.delete(review);
        log.info("[REVIEW_DELETE] reviewId={}, userId={}", reviewId, userId);
    }

    // ──────────────────────────────────────────────
    // 4. 공연별 후기 목록 (공개)
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByEvent(Long eventId) {
        return reviewRepository.findAllByEventIdOrderByCreatedAtDesc(eventId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    // ──────────────────────────────────────────────
    // 5. 내가 쓴 후기 (마이페이지)
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(Long userId) {
        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    // ──────────────────────────────────────────────
    // 6. 공연별 평균/총 개수
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(Long eventId) {
        Double avg = reviewRepository.findAverageRatingByEventId(eventId);
        long count = reviewRepository.countByEventId(eventId);
        double rounded = Math.round((avg == null ? 0 : avg) * 10.0) / 10.0;
        return new ReviewSummaryResponse(eventId, count, rounded);
    }

    // ──────────────────────────────────────────────
    // 7. 작성 가능 여부 체크
    //   - 공연 시작일이 지났어야 함
    //   - 해당 공연에 CONFIRMED 예매 내역이 있어야 함
    //   - 아직 작성한 적이 없어야 작성 가능 (있으면 수정 모드)
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ReviewEligibilityResponse checkEligibility(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다."));

        // 이미 작성했으면 그것 자체로 응답
        var existing = reviewRepository.findByUserIdAndEventId(userId, eventId);
        if (existing.isPresent()) {
            return ReviewEligibilityResponse.builder()
                    .eligible(false)
                    .alreadyWritten(true)
                    .existingReviewId(existing.get().getId())
                    .reason("이미 작성한 후기가 있습니다. 수정 가능합니다.")
                    .build();
        }

        if (event.getStartTime() == null || event.getStartTime().isAfter(LocalDateTime.now())) {
            return ReviewEligibilityResponse.builder()
                    .eligible(false)
                    .alreadyWritten(false)
                    .reason("공연 종료 후에 후기를 작성할 수 있어요.")
                    .build();
        }

        boolean hasReservation = hasConfirmedReservation(userId, eventId);
        if (!hasReservation) {
            return ReviewEligibilityResponse.builder()
                    .eligible(false)
                    .alreadyWritten(false)
                    .reason("이 공연을 예매한 분만 후기를 작성할 수 있어요.")
                    .build();
        }

        return ReviewEligibilityResponse.builder()
                .eligible(true)
                .alreadyWritten(false)
                .build();
    }

    // ──────────────────────────────────────────────
    // 내부 검증 헬퍼
    // ──────────────────────────────────────────────
    private void verifyEligible(User user, Event event) {
        if (event.getStartTime() == null || event.getStartTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("공연 종료 후에 후기를 작성할 수 있어요.");
        }
        if (!hasConfirmedReservation(user.getId(), event.getId())) {
            throw new RuntimeException("이 공연을 예매한 분만 후기를 작성할 수 있어요.");
        }
    }

    private boolean hasConfirmedReservation(Long userId, Long eventId) {
        return reservationRepository
                .findAllByUserIdAndStatusOrderByReservedAtDesc(userId, ReservationStatus.CONFIRMED)
                .stream()
                .anyMatch(r -> r.getSeat().getEvent().getId().equals(eventId));
    }
}
