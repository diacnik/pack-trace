package com.packtrace.mapper;

import com.packtrace.dto.ClosetGearResponse;
import com.packtrace.dto.ClosetRequest;
import com.packtrace.dto.ClosetResponse;
import com.packtrace.model.Closet;
import com.packtrace.model.ClosetGear;
import com.packtrace.model.Gear;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClosetMapper {
    
    public static ClosetResponse toResponse(Closet closet) {
        if (closet == null)
            return null;
        return new ClosetResponse(
                closet.getId(),
                closet.getName(),
                closet.getDescription()
        );
    }

    public static Closet toEntity(ClosetRequest request) {
        if (request == null)
            return null;
        Closet closet = new Closet();
        closet.setName(request.name());
        closet.setDescription(request.description());
        return closet;
    }

    public static ClosetGearResponse toGearResponse(ClosetGear closetGear, Gear gear) {
        if (closetGear == null || gear == null)
            return null;
        return new ClosetGearResponse(
                gear.getId(),
                gear.getName(),
                gear.getBrand(),
                gear.getWeightGrams(),
                closetGear.getQuantity()
        );
    }
}
