package com.packtrace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record GearRequest (
        @NotBlank(message = "Gear name is required")
        @Size(min = 1, max = 255, message = "Name must be 1-255 characters")
        String name,
        
        @Size(max = 255, message = "Brand cannot exceed 255 characters")
        String brand,
        
        @Min(value = 1, message = "Weight must be at least 1 gram")
        @Max(value = 50000, message = "Weight cannot exceed 50 kg")
        int weightGrams,
        
        @Size(max = 2000, message = "URL cannot exceed 2000 characters")
        String websiteURL,
        
        @Size(max = 100, message = "Category cannot exceed 100 characters")
        String category
) {
}
