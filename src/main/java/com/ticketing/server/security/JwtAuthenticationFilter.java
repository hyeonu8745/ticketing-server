package com.ticketing.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 클라이언트의 요청(Request) 헤더에서 통행증(JWT)을 꺼냅니다.
        String token = resolveToken(request);

        // 2. 통행증이 있고, 위조되지 않았다면 문을 열어줍니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 토큰에서 유저 ID를 뽑아냅니다.
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // Spring Security의 VIP 명단(Context)에 이 유저를 등록합니다.
            // (이제 서버 어디서든 이 유저가 누구인지 알 수 있습니다!)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터나 컨트롤러로 요청을 넘겨줍니다.
        filterChain.doFilter(request, response);
    }

    // "Bearer eyJhb..." 형태로 들어오는 토큰에서 "Bearer " 글자를 떼어내는 역할
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}