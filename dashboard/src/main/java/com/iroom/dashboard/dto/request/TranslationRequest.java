package com.iroom.dashboard.dto.request;

import java.util.List;

public record TranslationRequest(
	List<String> text,
	String target_lang
) {
}

