package com.iroom.dashboard.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TranslationResponse {
	private List<Translation> translations;
}