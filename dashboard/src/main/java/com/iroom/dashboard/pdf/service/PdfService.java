package com.iroom.dashboard.pdf.service;

import com.iroom.dashboard.dashboard.repository.DashBoardRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfService { // PDF로 변환

	private final DashBoardRepository dashBoardRepository;

	public byte[] generateDashboardPdf(String pdfTitle, String content)
		throws DocumentException, IOException {

		Document document = new Document();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		PdfWriter.getInstance(document, out);
		document.open();

		// 영어 전용: 내장 폰트 사용
		BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

		Font headerFont = new Font(baseFont, 14, Font.BOLD);
		Font bodyFont = new Font(baseFont, 12);

		// 제목
		Paragraph title = new Paragraph(pdfTitle, headerFont);
		title.setAlignment(Element.ALIGN_CENTER);
		title.setSpacingAfter(20f);
		document.add(title);

		// 본문 줄 단위 추가
		String[] lines = content.split("\n");
		for (String line : lines) {
			Paragraph paragraph = new Paragraph(line, bodyFont);
			paragraph.setSpacingAfter(10f);
			document.add(paragraph);
		}

		document.close();
		return out.toByteArray();
	}
}
