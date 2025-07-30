package com.iroom.user.common.annotation.validator;

import com.iroom.user.common.annotation.ValidPhone;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        String trimmedPhone = phone.trim();

        // 하이픈 없는 휴대폰 번호 패턴 (010-1234-5678 -> 01012345678)
        boolean isValidWithHyphen = trimmedPhone.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$");

        // 하이픈 포함 휴대폰 번호 패턴 (010-1234-5678, 010-123-4567)
        boolean isValidWithoutHyphen = trimmedPhone.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");

        if (!isValidWithHyphen && !isValidWithoutHyphen) {
            addConstraintViolation(context, "휴대폰 번호는 010-1234-5678 또는 01012345678 형식이어야 합니다");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}