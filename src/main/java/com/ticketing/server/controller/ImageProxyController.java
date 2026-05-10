package com.ticketing.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/proxy")
public class ImageProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * KOPIS http 이미지를 프록시하여 https로 제공
     * GET /api/proxy/image?url=http://www.kopis.or.kr/upload/...
     */
    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            // http만 허용 (보안상 kopis 도메인만)
            if (!url.startsWith("http://www.kopis.or.kr/")) {
                return ResponseEntity.badRequest().build();
            }

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, byte[].class
            );

            HttpHeaders headers = new HttpHeaders();
            // Content-Type 유지
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType != null) headers.setContentType(contentType);
            // 캐시 1일
            headers.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)));

            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);

        } catch (Exception e) {
            log.warn("[IMAGE_PROXY] 이미지 로드 실패: {}", url);
            return ResponseEntity.notFound().build();
        }
    }
}