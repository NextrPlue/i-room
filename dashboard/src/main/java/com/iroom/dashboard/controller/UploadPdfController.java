package com.iroom.dashboard.controller;

import com.iroom.dashboard.service.UploadPdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UploadPdfController {
	private final UploadPdfService uploadPdfService;

	@PostMapping("/upload-pdf")
	public ResponseEntity<String> uploadReport(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok("저장 성공: " + uploadPdfService.uploadReport(file).getBody());
	}

}
