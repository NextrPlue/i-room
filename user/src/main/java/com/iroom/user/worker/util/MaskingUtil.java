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

	// 휴대폰 마스킹: 가운데 4자리 * 처리
	public static String maskPhoneMiddle4(String phone) {
		if (phone == null || phone.isEmpty()) return "";
		String digits = phone.replaceAll("\\D", "");
		// 02 지역번호 케이스(최소 9자리: 02-XXXX-XXXX)
		if (digits.startsWith("02") && digits.length() >= 9) {
			return "02-****-" + digits.substring(digits.length() - 4);
		}
		// 일반/휴대폰 (10~11+자리)
		if (digits.length() >= 10) {
			String head = digits.substring(0, 3);
			String tail = digits.substring(digits.length() - 4);
			return head + "-****-" + tail;
		}
		// 길이 애매하면 숫자만 * 처리
		return phone.replaceAll("\\d", "*");
	}

	// 이메일: 앞 3글자만 노출, 이후 @앞까지 모두 * 처리
	public static String maskEmailFirst3(String email) {
		if (email == null || email.isEmpty()) return "";
		String[] parts = email.split("@", 2);
		String local = parts[0];
		String domain = parts.length > 1 ? parts[1] : "";
		String maskedLocal = local.length() <= 3
			? local + "*"
			: local.substring(0, 3) + "*".repeat(local.length() - 3);
		return domain.isEmpty() ? maskedLocal : maskedLocal + "@" + domain;
	}

}
