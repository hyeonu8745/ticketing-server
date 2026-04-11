package com.ticketing.server.controller;

import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // GET http://localhost:8080/api/events
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents() {
        List<EventResponse> responses = eventService.getAllEvents();
        return ResponseEntity.ok(responses);
    }
}