package com.packtrace.mapper;

import com.packtrace.dto.GearRequest;
import com.packtrace.dto.GearResponse;
import com.packtrace.model.Gear;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GearMapper {
    public static GearResponse toResponse(Gear gear) {
        if (gear == null)
            return null;
        return new GearResponse(
                gear.getId(),
                gear.getName(),
                gear.getBrand(),
                gear.getWeightGrams(),
                gear.getWebsiteURL(),
                gear.getCategory()
        );
    }

    public static Gear toEntity(GearRequest request) {
        if (request == null)
            return null;
        Gear gear = new Gear();
        gear.setName(request.name());
        gear.setBrand(request.brand());
        gear.setWeightGrams(request.weightGrams());
        gear.setWebsiteURL(request.websiteURL());
        gear.setCategory(request.category());
        return gear;
    }
}
