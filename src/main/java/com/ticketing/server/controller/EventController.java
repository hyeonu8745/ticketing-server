package com.ticketing.server.controller;

import com.ticketing.server.dto.DemandForecastResponse;
import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.dto.RecommendationResponse;
import com.ticketing.server.service.DemandForecastService;
import com.ticketing.server.service.EventService;
import com.ticketing.server.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final RecommendationService recommendationService;
    private final DemandForecastService demandForecastService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return ResponseEntity.ok(eventService.getEventsPaging(category, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = (authentication != null) ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(recommendationService.getRecommendations(id, userId));
    }

    @GetMapping("/{id}/forecast")
    public ResponseEntity<DemandForecastResponse> getForecast(@PathVariable Long id) {
        return ResponseEntity.ok(demandForecastService.getForecast(id));
    }
}