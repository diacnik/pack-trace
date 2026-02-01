package com.packtrace.service;

import com.packtrace.model.Account;
import com.packtrace.repository.AccountRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Transactional
    public Account getOrCreateAccount(String auth0Id, String username) {
        Optional<Account> existingAccount = accountRepository.findByAuth0Id(auth0Id);

        if (existingAccount.isPresent())
            return existingAccount.get();

        String finalUsername = username;
        List<String> conflicts = accountRepository.findAllUsernamesStartingWith(username);
        Set<String> takenUsernames = new HashSet<>(conflicts);

        int counter = 1;
        while (takenUsernames.contains(finalUsername)) {
            finalUsername = username + "-" + counter;
            counter++;
        }

        Account createdAccount = new Account(auth0Id, finalUsername);
        accountRepository.persist(createdAccount);
        return createdAccount;
    }

    public Optional<Account> findByAuth0Id(String auth0Id) {
        return accountRepository.findByAuth0Id(auth0Id);
    }
}
