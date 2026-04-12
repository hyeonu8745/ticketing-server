package com.ticketing.server.config;

import com.ticketing.server.domain.Category;
import com.ticketing.server.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1) // 🌟 가장 먼저 실행!
@RequiredArgsConstructor
public class CategoryDataLoader implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional // 🌟 확실하게 DB에 반영되도록 트랜잭션 추가
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("CONCERT", "콘서트"));
            categoryRepository.save(new Category("MUSICAL", "뮤지컬"));
            categoryRepository.save(new Category("THEATER", "연극"));
            categoryRepository.flush(); // 즉시 DB 반영
        }
    }
}