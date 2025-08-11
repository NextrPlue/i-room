package com.iroom.dashboard.service;

import com.iroom.dashboard.pdf.service.PdfService;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PdfServiceTest {

    @Autowired
    private PdfService pdfService;

    @Test
    @DisplayName("generateDashboardPdf - 성공")
    void generateDashboardPdf_Success() throws DocumentException, IOException {
        // given
        String pdfTitle = "Test Title";
        String content = "Test content\nNew line";

        // when
        byte[] result = pdfService.generateDashboardPdf(pdfTitle, content);

        // then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}