package com.ticketing.server.repository;

import com.ticketing.server.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name); // 🌟 이름으로 카테고리를 찾는 기능
}