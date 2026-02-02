package com.packtrace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PackGearRequest (
        @NotNull(message = "Gear ID is required")
        Long gearId,
        
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
}
