package com.packtrace.dto;

public record PackGearResponse (
        Long gearId,
        String name,
        String brand,
        int weightGrams,
        int quantity
) {
}
