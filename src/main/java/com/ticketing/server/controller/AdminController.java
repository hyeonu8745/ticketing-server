package com.ticketing.server.controller;

import com.ticketing.server.service.KopisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KopisService kopisService;

    @GetMapping("/sync")
    public String syncData(@RequestParam(defaultValue = "100") int count) {
        // 1. 기존 데이터 초기화 (필요 시)
        // kopisService.deleteAllExistingData();

        // 2. KOPIS 데이터 수집 호출 (count 만큼 가져옴)
        // 🌟 수정된 포인트: 이제 count 인자를 전달합니다.
        kopisService.fetchAndSaveLargeEvents(count);

        return "🎯 KOPIS 데이터 " + count + "개 동기화 요청이 완료되었습니다!";
    }
}