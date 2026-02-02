package com.packtrace.service;

import com.packtrace.dto.PackGearResponse;
import com.packtrace.mapper.PackMapper;
import com.packtrace.model.Account;
import com.packtrace.model.Gear;
import com.packtrace.model.Pack;
import com.packtrace.model.PackGear;
import com.packtrace.repository.PackRepository;
import com.packtrace.repository.PackGearRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PackService {

    @Inject
    PackRepository packRepository;

    @Inject
    PackGearRepository packGearRepository;

    @Inject
    AccountService accountService;

    @Inject
    GearService gearService;

    @Transactional
    public Pack createPack(String auth0Id, Pack pack) {
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found. Please login via /api/account first."));
        
        if (packRepository.existsByAccountIdAndName(account.getId(), pack.getName())) {
            throw new IllegalArgumentException("Pack with this name already exists");
        }
        
        pack.setAccountId(account.getId());
        packRepository.persist(pack);
        return pack;
    }

    @Transactional
    public void deletePack(Long id, String auth0Id) {
        Optional<Pack> existingOpt = packRepository.findById(id);
        if (existingOpt.isPresent()) {
            Pack existing = existingOpt.get();
            Account account = accountService.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new SecurityException("Account not found"));

            if (!existing.getAccountId().equals(account.getId())) {
                throw new SecurityException("You do not own this pack");
            }
            packRepository.deleteById(id);
        }
    }

    public List<Pack> getMyPacks(String auth0Id) {
        return accountService.findByAuth0Id(auth0Id)
                .map(account -> packRepository.findByAccountId(account.getId()))
                .orElse(List.of());
    }

    public Optional<Pack> getPackById(Long id) {
        return packRepository.findById(id);
    }

    @Transactional
    public Pack updatePack(Long id, Pack updatedPack, String auth0Id) {
        Optional<Pack> existingOpt = packRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Pack not found");
        }

        Pack existing = existingOpt.get();

        // Verify ownership
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));

        if (!existing.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this pack");
        }

        // Check for name conflict with other packs
        if (!existing.getName().equals(updatedPack.getName()) && 
            packRepository.existsByAccountIdAndName(account.getId(), updatedPack.getName())) {
            throw new IllegalArgumentException("Pack with this name already exists");
        }

        // Update fields
        existing.setName(updatedPack.getName());
        existing.setDescription(updatedPack.getDescription());

        packRepository.update(existing);
        return existing;
    }

    @Transactional
    public void addGearToPack(Long packId, Long gearId, int quantity, String auth0Id) {
        // Verify pack ownership
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!pack.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this pack");
        }

        // Verify gear exists
        gearService.getGearById(gearId)
                .orElseThrow(() -> new IllegalArgumentException("Gear not found"));

        packGearRepository.addGearToPack(packId, gearId, quantity);
    }

    @Transactional
    public void removeGearFromPack(Long packId, Long gearId, String auth0Id) {
        // Verify pack ownership
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!pack.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this pack");
        }

        packGearRepository.removeGearFromPack(packId, gearId);
    }

    public List<PackGearResponse> getPackGear(Long packId) {
        List<PackGear> packGears = packGearRepository.findGearInPack(packId);
        return packGears.stream()
                .map(packGear -> {
                    Gear gear = gearService.getGearById(packGear.getGearId())
                            .orElse(null);
                    return PackMapper.toGearResponse(packGear, gear);
                })
                .filter(response -> response != null)
                .toList();
    }

    @Transactional
    public void updatePackGearQuantity(Long packId, Long gearId, int quantity, String auth0Id) {
        // Verify pack ownership
        Pack pack = packRepository.findById(packId)
                .orElseThrow(() -> new IllegalArgumentException("Pack not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!pack.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this pack");
        }

        // Verify gear is in pack
        packGearRepository.getQuantity(packId, gearId)
                .orElseThrow(() -> new IllegalArgumentException("Gear is not in this pack"));

        if (quantity <= 0) {
            packGearRepository.removeGearFromPack(packId, gearId);
        } else {
            packGearRepository.updateQuantity(packId, gearId, quantity);
        }
    }
}
