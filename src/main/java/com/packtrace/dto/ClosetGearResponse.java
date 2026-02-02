package com.packtrace.dto;

public record ClosetGearResponse (
        Long gearId,
        String name,
        String brand,
        int weightGrams,
        int quantity
) {
}
