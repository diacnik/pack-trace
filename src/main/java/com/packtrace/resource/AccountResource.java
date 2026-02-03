package com.packtrace.resource;

import com.packtrace.mapper.AccountMapper;
import com.packtrace.model.Account;
import com.packtrace.service.AccountService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    public Response getAccount() {
        try {
            String auth0Id = jwt.getSubject();
            
            // Validate token has proper subject claim
            if (auth0Id == null || auth0Id.isBlank()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse("Invalid token: subject claim is missing or empty"))
                        .build();
            }

            // Use subject if preferred_username is missing
            String username = jwt.getClaim("preferred_username");
            if (username == null || username.isBlank()) {
                username = auth0Id;
            }

            Account account = accountService.getOrCreateAccount(auth0Id, username);
            return Response.ok(AccountMapper.toResponse(account)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Authorization failed: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to retrieve account"))
                    .build();
        }
    }

    public record ErrorResponse(String message) {}
}
