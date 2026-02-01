package com.packtrace.dto;

public record GearRequest (
        String name,
        String brand,
        int weightGrams,
        String websiteURL,
        String category
) {
}
