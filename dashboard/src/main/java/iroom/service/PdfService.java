package iroom.service;



import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import iroom.entity.DashBoard;
import iroom.repository.DashBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final DashBoardRepository dashBoardRepository;

    public byte[] generateDashboardPdf(String pdfTitle) throws DocumentException {
        List<DashBoard> dashboards = dashBoardRepository.findAll();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph title = new Paragraph(pdfTitle, headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        PdfPTable table = new PdfPTable(4); // id, metricType, metricValue, recordedAt
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 3, 2, 3});

        // Table Header
        Stream.of("ID", "Metric Type", "Metric Value", "Recorded At").forEach(col -> {
            PdfPCell header = new PdfPCell(new Phrase(col, headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(header);
        });

        // Table Rows
        for (DashBoard d : dashboards) {
            table.addCell(new PdfPCell(new Phrase(d.getId().toString(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(d.getMetricType(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(d.getMetricValue().toString(), bodyFont)));
            table.addCell(new PdfPCell(new Phrase(d.getRecordedAt().toString(), bodyFont)));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}
