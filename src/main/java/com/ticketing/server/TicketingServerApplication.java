package com.ticketing.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // KOPIS API 주기적 수집 등 스케줄러를 위해 남겨둡니다.
@SpringBootApplication
public class TicketingServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingServerApplication.class, args);
    }

    /* * 🚨 여기에 있던 @Bean CommandLineRunner 로직을 통째로 날렸습니다! 🚨
     * 이제 서버가 켜질 때 불필요한 test1@test.com 유저나 더미 공연을 만들지 않습니다.
     */
}