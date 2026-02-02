package com.packtrace.repository;

import com.packtrace.dto.ClosetGearResponse;

import java.util.List;
import java.util.Optional;

public interface ClosetGearRepository {
    void addGearToCloset(Long closetId, Long gearId, int quantity);
    void removeGearFromCloset(Long closetId, Long gearId);
    List<ClosetGearResponse> findGearInCloset(Long closetId);
    Optional<Integer> getQuantity(Long closetId, Long gearId);
    void updateQuantity(Long closetId, Long gearId, int quantity);
}
