package com.packtrace.repository;

import com.packtrace.model.Closet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClosetRepository {
    void persist(Closet closet);
    Optional<Closet> findById(Long id);
    List<Closet> findByAccountId(UUID accountId);
    void update(Closet closet);
    void deleteById(Long id);
    boolean existsByAccountIdAndName(UUID accountId, String name);
}
