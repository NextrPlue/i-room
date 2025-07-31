package com.iroom.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iroom.dashboard.dto.request.RiskManagementRequest;
import com.iroom.dashboard.dto.response.DangerAreaResponse;
import com.iroom.dashboard.service.RiskManagementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/risk-management")
public class RiskManagementController {
	private final RiskManagementService riskManagementService;

	@PostMapping(value = "dangerzone")
	public ResponseEntity<String> detectDangerZone(
		@RequestBody RiskManagementRequest riskManagementRequest) {
		riskManagementService.detectDangerZone(riskManagementRequest);
		return ResponseEntity.ok("ok");
	}

}
