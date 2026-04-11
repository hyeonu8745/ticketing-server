package com.ticketing.server.controller;

import com.ticketing.server.dto.SeatResponse;
import com.ticketing.server.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // GET http://localhost:8080/api/events/1/seats
    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long eventId) {
        List<SeatResponse> responses = seatService.getSeatsByEvent(eventId);
        return ResponseEntity.ok(responses);
    }
}