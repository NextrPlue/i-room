package com.iroom.dashboard.dto.response;

import java.util.List;

public record TranslationResponse(
	List<Translation> translations
) {
}
