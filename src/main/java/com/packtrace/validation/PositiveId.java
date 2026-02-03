package com.packtrace.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PositiveIdValidator.class)
@Documented
public @interface PositiveId {
    String message() default "ID must be a positive number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
