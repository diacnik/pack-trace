package com.packtrace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;

public record GearRequest (
        @NotBlank(message = "Gear name is required")
        String name,
        
        String brand,
        
        @Min(value = 1, message = "Weight must be at least 1 gram")
        @Max(value = 50000, message = "Weight cannot exceed 50 kg")
        int weightGrams,
        
        String websiteURL,
        
        String category
) {
}
