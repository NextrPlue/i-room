package com.iroom.user.common.annotation;

import com.iroom.user.common.annotation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface ValidPassword {
    String message() default "비밀번호 정책에 맞지 않습니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}