package com.packtrace.service;

import com.packtrace.dto.ClosetGearResponse;
import com.packtrace.mapper.ClosetMapper;
import com.packtrace.model.Account;
import com.packtrace.model.Closet;
import com.packtrace.model.ClosetGear;
import com.packtrace.model.Gear;
import com.packtrace.repository.ClosetRepository;
import com.packtrace.repository.ClosetGearRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClosetService {

    @Inject
    ClosetRepository closetRepository;

    @Inject
    ClosetGearRepository closetGearRepository;

    @Inject
    AccountService accountService;

    @Inject
    GearService gearService;

    @Transactional
    public Closet createCloset(String auth0Id, Closet closet) {
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found. Please login via /api/account first."));
        
        if (closetRepository.existsByAccountIdAndName(account.getId(), closet.getName())) {
            throw new IllegalArgumentException("Closet with this name already exists");
        }
        
        closet.setAccountId(account.getId());
        closetRepository.persist(closet);
        return closet;
    }

    @Transactional
    public void deleteCloset(Long id, String auth0Id) {
        Optional<Closet> existingOpt = closetRepository.findById(id);
        if (existingOpt.isPresent()) {
            Closet existing = existingOpt.get();
            Account account = accountService.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new SecurityException("Account not found"));

            if (!existing.getAccountId().equals(account.getId())) {
                throw new SecurityException("You do not own this closet");
            }
            closetRepository.deleteById(id);
        }
    }

    public List<Closet> getMyClosets(String auth0Id) {
        return accountService.findByAuth0Id(auth0Id)
                .map(account -> closetRepository.findByAccountId(account.getId()))
                .orElse(List.of());
    }

    public Optional<Closet> getClosetById(Long id) {
        return closetRepository.findById(id);
    }

    @Transactional
    public Closet updateCloset(Long id, Closet updatedCloset, String auth0Id) {
        Optional<Closet> existingOpt = closetRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Closet not found");
        }

        Closet existing = existingOpt.get();

        // Verify ownership
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));

        if (!existing.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this closet");
        }

        // Check for name conflict with other closets
        if (!existing.getName().equals(updatedCloset.getName()) && 
            closetRepository.existsByAccountIdAndName(account.getId(), updatedCloset.getName())) {
            throw new IllegalArgumentException("Closet with this name already exists");
        }

        // Update fields
        existing.setName(updatedCloset.getName());
        existing.setDescription(updatedCloset.getDescription());

        closetRepository.update(existing);
        return existing;
    }

    @Transactional
    public void addGearToCloset(Long closetId, Long gearId, int quantity, String auth0Id) {
        // Verify closet ownership
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new IllegalArgumentException("Closet not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!closet.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this closet");
        }

        // Verify gear exists and is owned by user
        Gear gear = gearService.getGearById(gearId)
                .orElseThrow(() -> new IllegalArgumentException("Gear not found"));
        
        if (!gear.getOwnerId().equals(account.getId())) {
            throw new SecurityException("You do not own this gear");
        }

        closetGearRepository.addGearToCloset(closetId, gearId, quantity);
    }

    @Transactional
    public void removeGearFromCloset(Long closetId, Long gearId, String auth0Id) {
        // Verify closet ownership
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new IllegalArgumentException("Closet not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!closet.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this closet");
        }

        closetGearRepository.removeGearFromCloset(closetId, gearId);
    }

    public List<ClosetGearResponse> getClosetGear(Long closetId) {
        List<ClosetGear> closetGears = closetGearRepository.findGearInCloset(closetId);
        return closetGears.stream()
                .map(closetGear -> {
                    Gear gear = gearService.getGearById(closetGear.getGearId())
                            .orElse(null);
                    return ClosetMapper.toGearResponse(closetGear, gear);
                })
                .filter(response -> response != null)
                .toList();
    }

    @Transactional
    public void updateClosetGearQuantity(Long closetId, Long gearId, int quantity, String auth0Id) {
        // Verify closet ownership
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new IllegalArgumentException("Closet not found"));
        
        Account account = accountService.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new SecurityException("Account not found"));
        
        if (!closet.getAccountId().equals(account.getId())) {
            throw new SecurityException("You do not own this closet");
        }

        // Verify gear is in closet
        closetGearRepository.getQuantity(closetId, gearId)
                .orElseThrow(() -> new IllegalArgumentException("Gear is not in this closet"));

        if (quantity <= 0) {
            closetGearRepository.removeGearFromCloset(closetId, gearId);
        } else {
            closetGearRepository.updateQuantity(closetId, gearId, quantity);
        }
    }
}
