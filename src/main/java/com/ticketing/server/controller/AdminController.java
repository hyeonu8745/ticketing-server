package com.ticketing.server.controller;

import com.ticketing.server.domain.ReservationStatus;
import com.ticketing.server.domain.Review;
import com.ticketing.server.domain.UserRole;
import com.ticketing.server.dto.ReviewResponse;
import com.ticketing.server.dto.admin.*;
import com.ticketing.server.service.AdminService;
import com.ticketing.server.service.KopisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * /api/admin/** 는 SecurityConfig 에서 hasRole("ADMIN") 으로 보호됩니다.
 * 따라서 컨트롤러 내부에서는 인증 체크를 별도로 하지 않습니다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final KopisService kopisService;

    // ════════════════════════════════════════════════════════════════
    // 📊 대시보드
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ════════════════════════════════════════════════════════════════
    // 🎭 공연 관리
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/events")
    public ResponseEntity<Page<AdminEventResponse>> getEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getEvents(keyword, page, size));
    }

    @PatchMapping("/events/{eventId}/hide")
    public ResponseEntity<?> hideEvent(@PathVariable Long eventId) {
        try {
            adminService.hideEvent(eventId);
            return ResponseEntity.ok(Map.of("message", "공연이 숨김 처리되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/events/{eventId}/show")
    public ResponseEntity<?> showEvent(@PathVariable Long eventId) {
        try {
            adminService.showEvent(eventId);
            return ResponseEntity.ok(Map.of("message", "공연이 다시 표시됩니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long eventId,
            @RequestBody AdminEventUpdateRequest req) {
        try {
            return ResponseEntity.ok(adminService.updateEvent(eventId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 🎫 예매 관리
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/reservations")
    public ResponseEntity<Page<AdminReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getReservations(status, keyword, page, size));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<?> forceCancel(
            @PathVariable Long reservationId,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        try {
            adminService.forceCancelReservation(reservationId, adminId);
            return ResponseEntity.ok(Map.of("message", "예매가 강제 취소되었고 환불 처리되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 👤 회원 관리
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getUsers(keyword, page, size));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable Long userId,
            @RequestParam UserRole role,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(adminService.changeUserRole(userId, adminId, role));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // 💬 후기 관리
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        List<Review> reviews = adminService.getAllReviews();
        return ResponseEntity.ok(reviews.stream().map(ReviewResponse::from).toList());
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        Long adminId = (Long) authentication.getPrincipal();
        try {
            adminService.deleteReview(reviewId, adminId);
            return ResponseEntity.ok(Map.of("message", "후기가 삭제되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // ⚙️ KOPIS 동기화 (기존)
    // ════════════════════════════════════════════════════════════════
    @GetMapping("/sync")
    public String syncData(@RequestParam(defaultValue = "100") int count) {
        kopisService.fetchAndSaveLargeEvents(count);
        return "🎯 KOPIS 데이터 " + count + "개 동기화 요청이 완료되었습니다!";
    }
}
