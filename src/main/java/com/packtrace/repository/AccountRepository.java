package com.packtrace.repository;

import com.packtrace.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByAuth0Id(String auth0Id);
    Optional<Account> findByUsername(String username);
    List<String> findAllUsernamesStartingWith(String prefix);
    void persist(Account account);
}
