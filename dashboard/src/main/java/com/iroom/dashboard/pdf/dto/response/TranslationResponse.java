package com.iroom.dashboard.pdf.dto.response;

import java.util.List;

public record TranslationResponse(
	List<Translation> translations
) {
}
