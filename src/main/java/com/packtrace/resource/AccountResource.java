package com.packtrace.resource;

import com.packtrace.dto.AccountResponse;
import com.packtrace.mapper.AccountMapper;
import com.packtrace.model.Account;
import com.packtrace.service.AccountService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/account")
@Authenticated
public class AccountResource {

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AccountResponse getAccount() {
        String auth0Id = jwt.getSubject();

        // Use subject if preferred_username is missing
        String username = jwt.getClaim("preferred_username");
        if (username == null) {
            username = auth0Id;
        }

        Account account = accountService.getOrCreateAccount(auth0Id, username);
        return AccountMapper.toResponse(account);
    }
}
