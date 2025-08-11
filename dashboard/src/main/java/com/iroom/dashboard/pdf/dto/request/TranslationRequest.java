package com.iroom.dashboard.pdf.dto.request;

import java.util.List;

public record TranslationRequest(
	List<String> text,
	String target_lang
) {
}

