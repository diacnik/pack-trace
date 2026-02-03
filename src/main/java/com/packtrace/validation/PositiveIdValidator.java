package com.packtrace.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PositiveIdValidator implements ConstraintValidator<PositiveId, Long> {
    
    @Override
    public void initialize(PositiveId annotation) {
    }
    
    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value > 0;
    }
}
