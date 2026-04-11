package com.ticketing.server.controller;

import com.ticketing.server.dto.QueueResponse;
import com.ticketing.server.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/status")
    public ResponseEntity<?> getQueueStatus(
            @RequestParam Long eventId,
            Authentication authentication) {

        // [안전장치] 토큰이 없거나 잘못된 경우 보호
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다. 다시 로그인해주세요.");
        }

        Long userId = (Long) authentication.getPrincipal();
        QueueResponse response = queueService.joinQueue(eventId, userId);

        return ResponseEntity.ok(response);
    }
}