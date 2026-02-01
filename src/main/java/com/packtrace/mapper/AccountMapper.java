package com.packtrace.mapper;

import com.packtrace.dto.AccountResponse;
import com.packtrace.model.Account;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountMapper {

    public static AccountResponse toResponse(Account account) {
        if (account == null)
            return null;

        return new AccountResponse(account.getId(), account.getUsername(), account.getBio());
    }
}
