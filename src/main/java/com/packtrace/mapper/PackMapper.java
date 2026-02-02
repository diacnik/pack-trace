package com.packtrace.mapper;

import com.packtrace.dto.PackRequest;
import com.packtrace.dto.PackResponse;
import com.packtrace.model.Pack;
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
}
