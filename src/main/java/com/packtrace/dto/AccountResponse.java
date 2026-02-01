package com.packtrace.dto;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String username,
        String bio
) {
}

