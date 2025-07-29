package com.iroom.dashboard.controller;

import com.iroom.dashboard.service.UploadPdfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadPdfController.class)
@Import(UploadPdfControllerTest.MockConfig.class)
class UploadPdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UploadPdfService uploadPdfService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UploadPdfService uploadPdfService() {
            return Mockito.mock(UploadPdfService.class);
        }
    }

    @Test
    @DisplayName("POST /upload-pdf - PDF 업로드 성공")
    void uploadReport_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test pdf content".getBytes());
        when(uploadPdfService.uploadReport(any())).thenReturn(ResponseEntity.ok("test.pdf"));

        // when & then
        mockMvc.perform(multipart("/upload-pdf").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("저장 성공: test.pdf"));
    }

    @Test
    @DisplayName("POST /upload-pdf - PDF 업로드 실패 (파일 없음)")
    void uploadReport_Fail_NoFile() throws Exception {
        // when & then
        mockMvc.perform(multipart("/upload-pdf"))
                .andExpect(status().isBadRequest());
    }
}