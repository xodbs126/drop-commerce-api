package com.xodbs126.dropcommerce.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = NonZeroValidator.class)
public @interface NonZero {

    String message() default "0은 입력할 수 없습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
