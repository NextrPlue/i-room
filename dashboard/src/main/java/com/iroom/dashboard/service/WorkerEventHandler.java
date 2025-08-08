package com.iroom.dashboard.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.iroom.dashboard.entity.WorkerInfoReadModel;
import com.iroom.dashboard.repository.WorkerInfoReadModelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkerEventHandler {
	private final WorkerInfoReadModelRepository workerInfoReadModelRepository;
	public void handle(String eventType, JsonNode dataNode) {
		try {
			switch (eventType) {
				case "WORKER_CREATED" -> createWorkerInfoReadModel(dataNode);//리드 모델 만들기.
			}

			log.info("Successfully processed worker event: {}", eventType);
		}catch (Exception e) {
			log.error("Failed to process worker event: {}", eventType, e);
		}
	}
	private void createWorkerInfoReadModel(JsonNode dataNode) {
		WorkerInfoReadModel workerInfoReadModel = WorkerInfoReadModel.builder().id(dataNode.get("workerId").asLong()).build();
		workerInfoReadModelRepository.save(workerInfoReadModel);
	}
}
