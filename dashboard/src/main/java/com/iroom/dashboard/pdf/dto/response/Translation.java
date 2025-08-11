package com.iroom.dashboard.pdf.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Translation {
	private String detected_source_language;
	private String text;
}