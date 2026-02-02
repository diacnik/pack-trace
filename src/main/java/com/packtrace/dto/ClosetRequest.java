package com.packtrace.dto;

import jakarta.validation.constraints.NotBlank;

public record ClosetRequest (
        @NotBlank(message = "Closet name is required")
        String name,
        
        String description
) {
}
