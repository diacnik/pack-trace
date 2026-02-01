package com.packtrace.repository;

import com.packtrace.model.Gear;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GearRepository {
    void persist(Gear gear);
    Optional<Gear> findById(Long id);
    List<Gear> findByOwnerId(UUID ownerId);
    void update(Gear gear);
    void deleteById(Long id);
}
