package com.ticketing.server.service;

import com.ticketing.server.dto.EventResponse;
import com.ticketing.server.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllByOrderByStartTimeAsc().stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getStartTime(),
                        event.getTotalSeats()
                ))
                .collect(Collectors.toList());
    }
}