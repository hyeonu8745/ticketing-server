package com.ticketing.server.controller;

import com.ticketing.server.dto.ReservationResponse;
import com.ticketing.server.service.QueueService;
import com.ticketing.server.service.ReservationFacade;
import com.ticketing.server.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationFacade reservationFacade;
    private final ReservationService reservationService;
    private final QueueService queueService;

    @PostMapping
    public ResponseEntity<String> reserveSeat(
            @RequestParam Long eventId,
            @RequestParam Long seatId,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        Long userId = (Long) authentication.getPrincipal();

        if (!queueService.isAllowedToReserve(eventId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("정상적인 대기열을 거치지 않았습니다.");
        }

        try {
            reservationFacade.reserveSeatWithLock(eventId, seatId, userId);
            return ResponseEntity.ok("🎉 예매 성공!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getMyReservations(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

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
}