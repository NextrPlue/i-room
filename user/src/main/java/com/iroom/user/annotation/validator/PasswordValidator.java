package com.iroom.user.annotation.validator;

import com.iroom.user.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        // 최소 8자, 최대 16자
        if (password.length() < 8 || password.length() > 16) {
            addConstraintViolation(context, "비밀번호는 8-16자여야 합니다");
            return false;
        }

        // 영문자, 숫자, 특수문자 각각 최소 1개
        if (!password.matches(".*[a-zA-Z].*")) {
            addConstraintViolation(context, "대소문자를 포함해야 합니다");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            addConstraintViolation(context, "숫자를 포함해야 합니다");
            return false;
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            addConstraintViolation(context, "특수문자(()<>“‘;제외)를 포함해야 합니다");
            return false;
        }

        // 연속된 문자 4개 이상 금지
        if (hasConsecutiveChars(password)) {
            addConstraintViolation(context, "연속 4자리 영문, 숫자는 사용할 수 없습니다");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean hasConsecutiveChars(String str) {
        for (int i = 0; i <= str.length() - 4; i++) {
            boolean consecutive = true;
            for (int j = 1; j < 4; j++) {
                if (str.charAt(i) != str.charAt(i + j)) {
                    consecutive = false;
                    break;
                }
            }
            if (consecutive) return true;
        }
        return false;
    }
}