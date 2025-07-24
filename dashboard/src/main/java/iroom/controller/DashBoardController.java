package iroom.controller;



import iroom.dto.DashBoardResponse;
import iroom.service.DashBoardService;
import iroom.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping(value="/dashBoards")
public class DashBoardController {
    private final DashBoardService dashBoardService;

    private final PdfService pdfService;
    //대시보드 조회하기
    @GetMapping (value ="/get-DashBoard", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DashBoardResponse> getDashBoard(@RequestParam("metricType")String metricType){
        DashBoardResponse dashBoardDto= dashBoardService.getDashBoard(metricType);
        return ResponseEntity.ok(dashBoardDto);
    }


    @GetMapping(
            value = "/export-report",
            produces = "application/json;charset=UTF-8"
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

    @GetMapping(
            value = "/create-improvement",
            produces = "application/json;charset=UTF-8"
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
