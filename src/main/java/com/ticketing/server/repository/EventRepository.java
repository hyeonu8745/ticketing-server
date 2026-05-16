package com.ticketing.server.repository;

import com.ticketing.server.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ──────────────────────────────────────────────
    // 일반 사용자용 — visible=true 만 조회
    // ──────────────────────────────────────────────

    List<Event> findAllByVisibleTrueOrderByStartTimeAsc();
    boolean existsByKopisEventId(String kopisEventId);

    // visible=true 전체 페이징
    Page<Event> findAllByVisibleTrue(Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category.name = :categoryName AND e.visible = true")
    Page<Event> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("""
        SELECT DISTINCT e FROM Event e
        WHERE e.visible = true
          AND (
                LOWER(e.title)       LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.location)    LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.category.displayName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(e.category.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<Event> findByKeywordAcrossFields(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT DISTINCT e FROM Event e
        WHERE e.category.name = :categoryName
          AND e.visible = true
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

    // ──────────────────────────────────────────────
    // 관리자용 — visible 무관, 전체 조회
    // ──────────────────────────────────────────────

    @Query("""
        SELECT e FROM Event e
        WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Event> findAllForAdmin(@Param("keyword") String keyword, Pageable pageable);

    long countByVisibleTrue();

    // 호환 유지용 (지워도 됨)
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:keyword% AND e.visible = true")
    Page<Event> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.category.name = :categoryName AND e.title LIKE %:keyword% AND e.visible = true")
    Page<Event> findByCategoryNameAndTitleContaining(@Param("categoryName") String categoryName, @Param("keyword") String keyword, Pageable pageable);
}
