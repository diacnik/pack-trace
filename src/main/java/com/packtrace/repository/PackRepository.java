package com.packtrace.repository;

import com.packtrace.model.Pack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PackRepository {
    void persist(Pack pack);
    Optional<Pack> findById(Long id);
    List<Pack> findByAccountId(UUID accountId);
    void update(Pack pack);
    void deleteById(Long id);
    boolean existsByAccountIdAndName(UUID accountId, String name);
}
