package com.ticketing.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long tokenExpiration;

    // yml 파일에서 설정한 비밀키와 만료 시간을 가져옵니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.expiration}") long tokenExpiration) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.tokenExpiration = tokenExpiration;
    }

    // 1. JWT 토큰 생성 메서드
    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenExpiration);

        return Jwts.builder()
                .setSubject(userId.toString()) // 토큰의 주인을 userId로 설정
                .claim("role", role)           // 권한 정보(USER, ADMIN) 추가
                .setIssuedAt(now)              // 발행 시간
                .setExpiration(validity)       // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 암호화 알고리즘
                .compact();
    }

    // 2. JWT 토큰 유효성 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 문제없이 풀리면 정상 토큰!
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // 3. 토큰에서 UserId 꺼내기 (나중에 예매할 때 "누가 예매했는지" 알아내기 위함)
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }
}