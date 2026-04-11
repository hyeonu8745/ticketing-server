package com.ticketing.server.repository;

import com.ticketing.server.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 공연 시작 시간(startTime) 순으로 전체 목록을 가져옵니다.
    List<Event> findAllByOrderByStartTimeAsc();
}