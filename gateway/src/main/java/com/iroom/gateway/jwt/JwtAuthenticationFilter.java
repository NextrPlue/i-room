package com.iroom.gateway.jwt;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Request URI: {}", request.getURI());

            if (isPublicRoute(request)) {
                return chain.filter(exchange);
            }

            String token = extractToken(request);
            if (token == null) {
                log.error("Authorization token is missing");
                return onError(exchange, "Token is missing", HttpStatus.UNAUTHORIZED);
            }

            if (!jwtTokenProvider.validateToken(token)) {
                log.error("Authorization token is invalid");
                return onError(exchange, "Token is invalid", HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtTokenProvider.getClaims(token);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            String userRole = claims.get("role", String.class);

            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Role", userRole)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    private boolean isPublicRoute(ServerHttpRequest request) {
        // 토큰 검증을 제외할 경로 목록
        List<String> publicRoutes = List.of(
                "/api/user/actuator",
                "/api/user/admins/login",
                "/api/user/admins/signup"
        );

        return publicRoutes.stream().anyMatch(route -> request.getURI().getPath().contains(route));
    }

    private String extractToken(ServerHttpRequest request) {
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);

        return response.setComplete();
    }

    public static class Config {
        // 설정이 필요하면 추가
    }
}
