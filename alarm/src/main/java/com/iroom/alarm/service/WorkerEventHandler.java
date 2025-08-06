package com.iroom.alarm.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iroom.alarm.entity.WorkerReadModel;
import com.iroom.alarm.repository.WorkerReadModelRepository;
import com.iroom.modulecommon.dto.event.WorkerEvent;
import com.iroom.modulecommon.exception.CustomException;
import com.iroom.modulecommon.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkerEventHandler {

	private final WorkerReadModelRepository workerReadModelRepository;
	private final ObjectMapper objectMapper;

	public void handle(String eventType, JsonNode dataNode) {
		try {
			WorkerEvent workerEvent = objectMapper.treeToValue(dataNode, WorkerEvent.class);

			switch (eventType) {
				case "WORKER_CREATED" -> createWorkerReadModel(workerEvent);
				case "WORKER_UPDATED" -> updateWorkerReadModel(workerEvent);
				case "WORKER_DELETED" -> deleteWorkerReadModel(workerEvent.id());
			}
			log.info("Successfully processed worker event: {}", eventType);
		} catch (Exception e) {
			log.error("Failed to process worker event: {}", eventType, e);
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
			.orElseThrow(() -> new CustomException(ErrorCode.MANAGEMENT_WORKER_NOT_FOUND));

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