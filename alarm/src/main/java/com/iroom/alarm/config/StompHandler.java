package com.iroom.alarm.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.iroom.modulecommon.jwt.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
			String sessionId = accessor.getSessionId();
			userSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String jwt = extractToken(accessor);
			if (!StringUtils.hasText(jwt) || !jwtTokenProvider.validateToken(jwt)) {
				throw new SecurityException("유효하지 않은 토큰으로 웹소켓에 연결할 수 없습니다.");
			}

			Claims claims = jwtTokenProvider.getClaims(jwt);
			String userId = claims.getSubject();

			userSessionMap.put(userId, accessor.getSessionId());
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

	public String getSessionIdByUserId(String userId) {
		return userSessionMap.get(userId);
	}
}