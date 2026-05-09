package com.ticketing.server.repository;

import com.ticketing.server.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByStartTimeAsc();
    boolean existsByKopisEventId(String kopisEventId);

    // 카테고리만 있을 때
    @Query("SELECT e FROM Event e WHERE e.category.name = :categoryName")
    Page<Event> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    // ✅ 통합 검색: 제목 + 장소 + 설명 + 카테고리 displayName 모두 검색
    @Query("""
        SELECT DISTINCT e FROM Event e
        WHERE LOWER(e.title)       LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.location)    LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.category.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(e.category.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Event> findByKeywordAcrossFields(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 카테고리 + 통합 검색
    @Query("""
        SELECT DISTINCT e FROM Event e
        WHERE e.category.name = :categoryName
          AND (
                LOWER(e.title)       LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.location)    LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.category.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<Event> findByCategoryNameAndKeyword(
            @Param("categoryName") String categoryName,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 기존 단일 필드 검색 — 호환용 (지워도 됨)
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:keyword%")
    Page<Event> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category.name = :categoryName AND e.title LIKE %:keyword%")
    Page<Event> findByCategoryNameAndTitleContaining(@Param("categoryName") String categoryName, @Param("keyword") String keyword, Pageable pageable);
}