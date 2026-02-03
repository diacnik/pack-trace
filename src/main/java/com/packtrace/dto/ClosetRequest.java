package com.packtrace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClosetRequest (
        @NotBlank(message = "Closet name is required")
        @Size(min = 1, max = 255, message = "Name must be 1-255 characters")
        String name,
        
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description
) {
}
