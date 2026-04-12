package com.ticketing.server.controller;

import com.ticketing.server.service.KopisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KopisService kopisService;

    // 🚀 브라우저에서 이 주소를 치면 KOPIS 데이터가 우리 DB로 들어옵니다!
    @GetMapping("/sync")
    public String syncData() {
        kopisService.fetchAndSaveLargeEvents();
        return "🎯 KOPIS 데이터 동기화 성공! 인텔리제이 로그를 확인하세요.";
    }
}