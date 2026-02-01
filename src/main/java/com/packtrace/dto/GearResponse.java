package com.packtrace.dto;

public record GearResponse(
        Long id,
        String name,
        String brand,
        int weightGrams,
        String websiteURL,
        String category
) {
}
