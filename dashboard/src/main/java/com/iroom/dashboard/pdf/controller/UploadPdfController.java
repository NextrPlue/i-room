package com.iroom.dashboard.pdf.controller;

import com.iroom.dashboard.pdf.service.UploadPdfService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UploadPdfController {
	private final UploadPdfService uploadPdfService;

	@PostMapping("/upload-pdf")
	public ResponseEntity<String> uploadReport(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok("저장 성공: " + uploadPdfService.uploadReport(file).getBody());
	}

}
