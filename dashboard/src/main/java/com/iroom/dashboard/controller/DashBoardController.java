package com.iroom.dashboard.controller;






import com.iroom.dashboard.dto.response.DashBoardResponse;
import com.iroom.dashboard.service.DashBoardService;
import com.iroom.dashboard.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping(value="/dashboards")
public class DashBoardController {
    private final DashBoardService dashBoardService;

    private final PdfService pdfService;
    //대시보드 조회하기
    @GetMapping (value ="/{metricType}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DashBoardResponse> getDashBoard(@PathVariable String metricType){
        DashBoardResponse dashBoardDto= dashBoardService.getDashBoard(metricType);
        return ResponseEntity.ok(dashBoardDto);
    }

    //리포트 생성
    @PostMapping(
            value = "/report"
    )
    public ResponseEntity<byte[]> exportReport() throws Exception {
        LocalDate currentDate = LocalDate.now();
        byte[] pdfBytes = pdfService.generateDashboardPdf("report_"+currentDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("report_"+currentDate+".pdf") //
                .build());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    //개선안 생성
    @PostMapping(
            value = "/improvement-report"
    )
    public ResponseEntity<byte[]> createImprovement(
    ) throws Exception {
        LocalDate currentDate = LocalDate.now();
        byte[] pdfBytes = pdfService.generateDashboardPdf("improvement_report_"+currentDate);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("improvement_report_"+currentDate+".pdf")
                .build());
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}