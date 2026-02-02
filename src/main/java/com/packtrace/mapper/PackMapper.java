package com.packtrace.mapper;

import com.packtrace.dto.PackGearResponse;
import com.packtrace.dto.PackRequest;
import com.packtrace.dto.PackResponse;
import com.packtrace.model.Gear;
import com.packtrace.model.Pack;
import com.packtrace.model.PackGear;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PackMapper {
    
    public static PackResponse toResponse(Pack pack) {
        if (pack == null)
            return null;
        return new PackResponse(
                pack.getId(),
                pack.getName(),
                pack.getDescription()
        );
    }

    public static Pack toEntity(PackRequest request) {
        if (request == null)
            return null;
        Pack pack = new Pack();
        pack.setName(request.name());
        pack.setDescription(request.description());
        return pack;
    }

    public static PackGearResponse toGearResponse(PackGear packGear, Gear gear) {
        if (packGear == null || gear == null)
            return null;
        return new PackGearResponse(
                gear.getId(),
                gear.getName(),
                gear.getBrand(),
                gear.getWeightGrams(),
                packGear.getQuantity()
        );
    }
}
