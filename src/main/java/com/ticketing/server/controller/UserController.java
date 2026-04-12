package com.ticketing.server.controller;

import com.ticketing.server.domain.User;
import com.ticketing.server.dto.UserUpdateRequest;
import com.ticketing.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");

        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody UserUpdateRequest request, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");

        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateName(request.getName());
        userRepository.save(user);
        return ResponseEntity.ok("정보 수정이 완료되었습니다.");
    }

    // 🌟 신규 추가: 포인트 충전 API
    @PostMapping("/charge")
    public ResponseEntity<String> chargePoint(@RequestParam Long amount, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");

        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.chargePoint(amount); // User 엔티티의 충전 메서드 호출
        userRepository.save(user);

        return ResponseEntity.ok("포인트가 성공적으로 충전되었습니다.");
    }
}