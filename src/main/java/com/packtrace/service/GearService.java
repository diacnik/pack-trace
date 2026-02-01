package com.packtrace.service;

import com.packtrace.model.Account;
import com.packtrace.model.Gear;
import com.packtrace.repository.GearRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GearService {

    @Inject
    GearRepository gearRepository;

    @Inject
    AccountService accountService;

    @Transactional
    public Gear createGear(String auth0Id, Gear gear) {
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found. Please login via /api/me first."));
        gear.setOwnerId(account.getId());
        gearRepository.persist(gear);
        return gear;
    }

    @Transactional
    public void deleteGear(Long id, String auth0Id) {
        Optional<Gear> existingOpt = gearRepository.findById(id);
        if (existingOpt.isPresent()) {
            Gear existing = existingOpt.get();
            Account account = accountService.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new SecurityException("Account not found"));

            if (!existing.getOwnerId().equals(account.getId())) {
                throw new SecurityException("You do not own this gear");
            }
            gearRepository.deleteById(id);
        }
    }

    public List<Gear> getMyGear(String auth0Id) {
        return accountService.findByAuth0Id(auth0Id)
                .map(account -> gearRepository.findByOwnerId(account.getId()))
                .orElse(List.of());
    }

    public Optional<Gear> getGearById(Long id) {
        return gearRepository.findById(id);
    }

    @Transactional
    public Gear updateGear(Long id, Gear updatedGear, String auth0Id) {
        Optional<Gear> existingOpt = gearRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Gear not found");
        }

        Gear existing = existingOpt.get();

        // Verify ownership
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));

        if (!existing.getOwnerId().equals(account.getId())) {
            throw new SecurityException("You do not own this gear");
        }

        // Update fields
        existing.setName(updatedGear.getName());
        existing.setBrand(updatedGear.getBrand());
        existing.setWeightGrams(updatedGear.getWeightGrams());
        existing.setWebsiteURL(updatedGear.getWebsiteURL());
        existing.setCategory(updatedGear.getCategory());

        gearRepository.update(existing);
        return existing;
    }
}
