package com.iroom.management.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.modulecommon.dto.event.WorkerEvent;
import com.iroom.management.entity.WorkerReadModel;
import com.iroom.management.repository.WorkerReadModelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkerEventListener {

	private final WorkerReadModelRepository workerReadModelRepository;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = "iroom", groupId = "management-service")
	public void handleWorkerEvent(String message) {
		try {
			JsonNode eventNode = objectMapper.readTree(message);
			String eventType = eventNode.get("eventType").asText();
			JsonNode dataNode = eventNode.get("data");

			log.info("Received Kafka message: eventType={}, data={}", eventType, dataNode);

			WorkerEvent workerEvent = objectMapper.treeToValue(dataNode, WorkerEvent.class);

			switch (eventType) {
				case "WORKER_CREATED" -> createWorkerReadModel(workerEvent);
				case "WORKER_UPDATED" -> updateWorkerReadModel(workerEvent);
				case "WORKER_DELETED" -> deleteWorkerReadModel(workerEvent.id());
				default -> log.warn("Unknown event type: {}", eventType);
			}
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", message, e);
		}
	}

	private void createWorkerReadModel(WorkerEvent event) {
		if (workerReadModelRepository.existsById(event.id())) {
			log.warn("Worker with id {} already exists in read model", event.id());
			return;
		}

		WorkerReadModel readModel = WorkerReadModel.builder()
			.id(event.id())
			.name(event.name())
			.email(event.email())
			.phone(event.phone())
			.role(event.role())
			.bloodType(event.bloodType())
			.gender(event.gender())
			.age(event.age())
			.weight(event.weight())
			.height(event.height())
			.jobTitle(event.jobTitle())
			.occupation(event.occupation())
			.department(event.department())
			.faceImageUrl(event.faceImageUrl())
			.createdAt(event.createdAt())
			.updatedAt(event.updatedAt())
			.build();

		workerReadModelRepository.save(readModel);
		log.info("Created worker read model for worker id: {}", event.id());
	}

	private void updateWorkerReadModel(WorkerEvent event) {
		WorkerReadModel readModel = workerReadModelRepository.findById(event.id())
			.orElseThrow(() -> new IllegalArgumentException("Worker read model not found: " + event.id()));

		readModel.updateFromEvent(event);
		workerReadModelRepository.save(readModel);
		log.info("Updated worker read model for worker id: {}", event.id());
	}

	private void deleteWorkerReadModel(Long workerId) {
		if (workerReadModelRepository.existsById(workerId)) {
			workerReadModelRepository.deleteById(workerId);
			log.info("Deleted worker read model for worker id: {}", workerId);
		} else {
			log.warn("Worker read model not found for deletion: {}", workerId);
		}
	}
}