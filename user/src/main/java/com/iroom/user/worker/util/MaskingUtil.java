package com.iroom.user.worker.util;

public class MaskingUtil {

	// 이름 마스킹: 가운데 한 글자만 * 처리
	public static String maskNameMiddleOne(String name) {
		if (name == null || name.isEmpty()) return "";
		int len = name.length();
		if (len == 1) return "*";
		if (len == 2) return name.charAt(0) + "*";
		int mid = len / 2;
		StringBuilder sb = new StringBuilder(name);
		sb.setCharAt(mid, '*');
		return sb.toString();
	}

}
