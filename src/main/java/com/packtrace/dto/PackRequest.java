package com.packtrace.dto;

import jakarta.validation.constraints.NotBlank;

public record PackRequest (
        @NotBlank(message = "Pack name is required")
        String name,
        
        String description
) {
}
