package com.packtrace.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        
        @NotBlank(message = "Username is required")
        String username,
        
        String bio
) {
}

