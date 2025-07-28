package com.iroom.dashboard.service;

import com.iroom.dashboard.entity.DashBoard;
import com.iroom.dashboard.repository.DashBoardRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PdfService { //PDF로 변환

	private final DashBoardRepository dashBoardRepository;

	public byte[] generateDashboardPdf(String pdfTitle, String content) throws
		DocumentException,
		IOException,
		IOException {
		Document document = new Document();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		PdfWriter.getInstance(document, out);
		document.open();

		// 한글 폰트 설정
		String fontPath = "/System/Library/Fonts/Supplemental/AppleGothic.ttf"; // mac은 "/Library/Fonts/AppleGothic.ttf"
		BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
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