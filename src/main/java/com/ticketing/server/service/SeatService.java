package com.ticketing.server.service;

import com.ticketing.server.dto.SeatResponse;
import com.ticketing.server.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByEvent(Long eventId) {
        return seatRepository.findAllByEventId(eventId).stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getSeatNumber(),
                        seat.getStatus(),
                        seat.getPrice()
                ))
                .toList();
    }
}