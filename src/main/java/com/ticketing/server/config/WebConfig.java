package com.ticketing.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3001",
                        "https://jihyeonu.com",
                        "http://www.jihyeonu.com",   // 👈 추가
                        "https://www.jihyeonu.com",  // 👈 추가
                        "https://api.jihyeonu.com",
                        "https://monitor.jihyeonu.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // OPTIONS 추가 권장
                .allowCredentials(true);
    }
}
