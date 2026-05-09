package com.ticketing.server.config;

import com.ticketing.server.domain.Category;
import com.ticketing.server.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1) // 🌟 모든 로직 중 가장 먼저 실행되어 기반을 잡습니다.
@RequiredArgsConstructor
public class CategoryDataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional // 트랜잭션을 보장하여 save 후 flush가 즉시 반영되게 합니다.
    public void run(String... args) {
        // 테이블이 비어있을 때만 실행 (중복 방지)
        if (categoryRepository.count() == 0) {

            // 🌟 빌더 패턴을 적용하여 데이터 매핑의 명확성을 높였습니다.
            categoryRepository.save(Category.builder()
                    .name("CONCERT")
                    .displayName("콘서트")
                    .build());

            categoryRepository.save(Category.builder()
                    .name("MUSICAL")
                    .displayName("뮤지컬")
                    .build());

            categoryRepository.save(Category.builder()
                    .name("THEATER")
                    .displayName("연극")
                    .build());

            categoryRepository.save(Category.builder()
                    .name("VISIT") // 식별 코드: VISIT (또는 INTERNATIONAL)
                    .displayName("내한공연")
                    .build());

            categoryRepository.flush(); // 영속성 컨텍스트의 내용을 DB에 즉시 반영
        }
    }
}