package com.ticketing.server.service;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 검색어 동의어 / 별칭 확장기
 *
 * 사용자가 "콘서트" 라고 검색해도 카테고리 코드 "CONCERT"가 들어간 공연을
 * 찾을 수 있도록, 입력 키워드를 여러 동의어로 확장한다.
 *
 * 예) "콘서트" → ["콘서트", "concert", "CONCERT", "공연"]
 *      "BTS"   → ["BTS", "방탄소년단", "방탄"]
 */
@Component
public class SearchKeywordExpander {

    // 카테고리/장르 매핑 (정확 매칭 시 동의어 추가)
    private static final Map<String, List<String>> SYNONYMS = new HashMap<>();

    static {
        // ── 카테고리 동의어 ──
        addSynonym("콘서트",   "concert", "CONCERT", "라이브");
        addSynonym("뮤지컬",   "musical", "MUSICAL");
        addSynonym("연극",     "theater", "THEATER", "공연", "play");
        addSynonym("내한공연", "visit",   "VISIT", "내한", "해외공연");
        addSynonym("클래식",   "classic", "CLASSIC", "오케스트라");

        // ── 자주 검색되는 아티스트/공연 별칭 ──
        addSynonym("BTS",      "방탄소년단", "방탄", "bangtan");
        addSynonym("아이유",   "IU", "iu");
        addSynonym("블랙핑크", "BLACKPINK", "blackpink", "blink");
        addSynonym("뉴진스",   "NewJeans", "newjeans");

        // ── 지역명 동의어 ──
        addSynonym("서울",     "seoul");
        addSynonym("부산",     "busan");
        addSynonym("강남",     "강남구");
        addSynonym("올림픽공원", "올림픽홀", "체조경기장", "KSPO");
        addSynonym("잠실",     "잠실종합운동장", "주경기장");

        // ── 장르/분위기 ──
        addSynonym("재즈",     "jazz", "JAZZ");
        addSynonym("힙합",     "hiphop", "hip-hop", "랩");
        addSynonym("락",       "rock", "ROCK", "락밴드");
        addSynonym("발레",     "ballet", "BALLET");
    }

    /**
     * 양방향 동의어 등록.
     * "콘서트" ↔ "concert" 처럼 어느 쪽으로 검색해도 다른 키워드까지 함께 찾아짐.
     */
    private static void addSynonym(String key, String... values) {
        List<String> all = new ArrayList<>();
        all.add(key);
        all.addAll(Arrays.asList(values));

        for (String word : all) {
            String lower = word.toLowerCase();
            SYNONYMS.computeIfAbsent(lower, k -> new ArrayList<>())
                    .addAll(all.stream().filter(w -> !w.equalsIgnoreCase(word)).toList());
        }
    }

    /**
     * 키워드 확장 — 입력어와 그 동의어들을 모두 반환.
     * 동의어가 없으면 입력어만 단독으로 반환.
     */
    public List<String> expand(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        String trimmed = keyword.trim();
        String lower = trimmed.toLowerCase();

        Set<String> result = new LinkedHashSet<>();
        result.add(trimmed); // 원본 키워드 우선

        if (SYNONYMS.containsKey(lower)) {
            result.addAll(SYNONYMS.get(lower));
        }

        return new ArrayList<>(result);
    }
}