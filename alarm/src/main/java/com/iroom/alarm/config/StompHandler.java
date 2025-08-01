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
	private final Map<String, String> sessionRoleMap = new ConcurrentHashMap<>();

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

		if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
			String destination = accessor.getDestination();
			String sessionId = accessor.getSessionId();
			String userRole = sessionRoleMap.get(sessionId);

			validateSubscription(destination, userRole);
		}

		if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
			String sessionId = accessor.getSessionId();
			userSessionMap.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
			sessionRoleMap.remove(sessionId);
		}

		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String jwt = extractToken(accessor);
			if (!StringUtils.hasText(jwt) || !jwtTokenProvider.validateToken(jwt)) {
				throw new SecurityException("유효하지 않은 토큰으로 웹소켓에 연결할 수 없습니다.");
			}

			Claims claims = jwtTokenProvider.getClaims(jwt);
			String userId = claims.getSubject();
			String role = claims.get("role", String.class);

			userSessionMap.put(userId, accessor.getSessionId());
			sessionRoleMap.put(accessor.getSessionId(), role);
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

	private void validateSubscription(String destination, String userRole) {
		if (destination == null || userRole == null) {
			throw new SecurityException("구독 권한이 없습니다.");
		}

		// 관리자 전용 토픽 접근 제한
		if (destination.equals("/topic/alarms/admin")) {
			if (!"ROLE_SUPER_ADMIN".equals(userRole) &&
				!"ROLE_ADMIN".equals(userRole) &&
				!"ROLE_READER".equals(userRole)) {
				throw new SecurityException("관리자 권한이 필요합니다.");
			}
		}

		// 근로자 개별 큐 접근 제한
		if (destination.startsWith("/queue/alarms-")) {
			if (!"ROLE_WORKER".equals(userRole)) {
				throw new SecurityException("근로자 권한이 필요합니다.");
			}
		}
	}
}