package com.iroom.dashboard.service;

import java.time.LocalDateTime;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.iroom.dashboard.dto.event.AbstractEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

	private final StreamBridge streamBridge;

	public void publishMessage(String eventType, Object data) {
		AbstractEvent event = new AbstractEvent(
			eventType,
			LocalDateTime.now(),
			data
		);

		streamBridge.send("event-out", event);
	}
}