package com.iroom.alarm.config;

import java.util.Collections;

import com.iroom.modulecommon.jwt.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String jwt = extractToken(accessor);
			if (!StringUtils.hasText(jwt) || !jwtTokenProvider.validateToken(jwt)) {
				throw new SecurityException("유효하지 않은 토큰으로 웹소켓에 연결할 수 없습니다.");
			}
			Authentication authentication = getAuthentication(jwt);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			accessor.setUser(authentication);
		}

		return message;
	}

	private String extractToken(StompHeaderAccessor accessor) {
		String bearerToken = accessor.getFirstNativeHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private Authentication getAuthentication(String token) {
		Claims claims = jwtTokenProvider.getClaims(token);
		String userId = claims.getSubject();
		String role = claims.get("role", String.class);

		return new UsernamePasswordAuthenticationToken(userId, null,
			Collections.singleton(new SimpleGrantedAuthority(role)));
	}
}