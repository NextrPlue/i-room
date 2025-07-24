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
@RequestMapping(value="/api")
public class DashBoardController {
    private final DashBoardService dashBoardService;

    private final PdfService pdfService;
    //대시보드 조회하기
    @GetMapping (value ="/dashBoards/getDashBoard", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DashBoardResponse> getDashBoard(@RequestParam("metricType")String metricType){
        DashBoardResponse dashBoardDto= dashBoardService.getDashBoard(metricType);
        return new ResponseEntity<>(dashBoardDto, HttpStatus.OK);
    }


    @GetMapping(
            value = "/dashBoards/exportreport",
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
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping(
            value = "/dashBoards/createimprovement",
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
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
