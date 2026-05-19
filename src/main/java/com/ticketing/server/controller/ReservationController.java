package com.ticketing.server.controller;

import com.ticketing.server.dto.BotDetectionRequest;
import com.ticketing.server.service.BotDetectionService;
import com.ticketing.server.service.QueueService;
import com.ticketing.server.service.ReservationFacade;
import com.ticketing.server.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;
    private final ReservationService reservationService;
    private final QueueService queueService;
    private final BotDetectionService botDetectionService;

    // ──────────────────────────────────────────────
    // 1. 좌석 예매 (봇 탐지 통합)
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<String> reserveSeat(
            @RequestParam Long eventId,
            @RequestParam Long seatId,
            @RequestParam(defaultValue = "0") double queueWaitSeconds,
            @RequestParam(defaultValue = "true") boolean hasInteraction,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        Long userId = (Long) authentication.getPrincipal();

        // ── Step 1: GraphSAGE 봇 탐지 ──────────────────
        String clientIp = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // 유저 취소/성공 횟수 조회는 Service 계층 위임
        int cancelCount = reservationService.countCancelledByUser(userId);
        int successCount = reservationService.countConfirmedByUser(userId);

        BotDetectionRequest botRequest = BotDetectionRequest.builder()
                .userId(userId)
                .eventId(eventId)
                .ipAddress(clientIp)
                .userAgent(userAgent != null ? userAgent : "unknown")
                .queueWaitSeconds(queueWaitSeconds)
                .hasInteraction(hasInteraction)
                .cancelCount(cancelCount)
                .successCount(successCount)
                .build();

        if (botDetectionService.isBot(botRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("🚨 비정상적인 접근이 감지되었습니다. 잠시 후 다시 시도해주세요.");
        }
        // ── 봇 탐지 끝 ────────────────────────────────────

        // ── Step 2: 대기열 검증 (기존 로직 유지) ──────────
        boolean isAllowed = false;
        for (int i = 0; i < 3; i++) {
            if (queueService.isAllowedToReserve(eventId, userId)) {
                isAllowed = true;
                break;
            }
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (!isAllowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("정상적인 대기열을 거치지 않았습니다.");
        }

        // ── Step 3: 실제 예매 처리 ─────────────────────────
        try {
            reservationFacade.reserveSeatWithLock(eventId, seatId, userId);
            return ResponseEntity.ok("🎉 예매 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 2. 내 예매 내역 조회 (기존과 동일)
    // ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getMyReservations(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    // ──────────────────────────────────────────────
    // 3. 예매 취소 (기존과 동일)
    // ──────────────────────────────────────────────
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Long reservationId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        Long userId = (Long) authentication.getPrincipal();

        try {
            reservationService.cancelReservation(reservationId, userId);
            return ResponseEntity.ok("예약이 정상적으로 취소되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 4. 좌석 변경 (기존과 동일)
    // ──────────────────────────────────────────────
    @PutMapping("/{reservationId}/seats/{newSeatId}")
    public ResponseEntity<String> changeSeat(
            @PathVariable Long reservationId,
            @PathVariable Long newSeatId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        try {
            reservationService.changeSeat(reservationId, newSeatId, userId);
            return ResponseEntity.ok("좌석 변경 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // 유틸: 실제 클라이언트 IP 추출 (Proxy/LB 고려)
    // ──────────────────────────────────────────────
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim(); // 다중 프록시인 경우 첫 번째 IP
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) return ip;
        return request.getRemoteAddr();
    }
}