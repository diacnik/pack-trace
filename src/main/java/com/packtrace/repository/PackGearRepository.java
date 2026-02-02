package com.packtrace.repository;

import com.packtrace.model.PackGear;

import java.util.List;
import java.util.Optional;

public interface PackGearRepository {
    void addGearToPack(Long packId, Long gearId, int quantity);
    void removeGearFromPack(Long packId, Long gearId);
    List<PackGear> findGearInPack(Long packId);
    Optional<Integer> getQuantity(Long packId, Long gearId);
    void updateQuantity(Long packId, Long gearId, int quantity);
}
