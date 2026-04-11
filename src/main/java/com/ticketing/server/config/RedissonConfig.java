package com.ticketing.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 6GB 서버 환경에서 스레드 경합과 메모리 효율을 고려한 설정입니다.
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                // 연결이 없을 때 유지할 최소 연결 수
                .setConnectionMinimumIdleSize(5)
                // 부하가 몰릴 때 최대로 늘어날 수 있는 연결 수 (6GB에선 20~32 정도가 적당합니다)
                .setConnectionPoolSize(20)
                // 연결 시도 타임아웃 (3초)
                .setConnectTimeout(3000)
                // 명령 실행 타임아웃 (3초)
                .setTimeout(3000);

        return Redisson.create(config);
    }
}