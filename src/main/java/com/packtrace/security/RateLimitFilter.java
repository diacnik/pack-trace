package com.packtrace.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RateLimitFilter implements ContainerRequestFilter {

    private static final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    private static final Bandwidth GENERAL_LIMIT = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
    private static final Bandwidth AUTH_LIMIT = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        
        // Skip rate limiting for public endpoints
        if (path.equals("account")) {
            return;
        }

        String clientId = getClientIdentifier(requestContext);
        Bucket bucket;

        if (path.contains("account")) {
            // Strict limit for account operations
            bucket = cache.computeIfAbsent(clientId + ":auth", k -> Bucket.builder()
                    .addLimit(AUTH_LIMIT)
                    .build());
        } else {
            // General limit for all other endpoints
            bucket = cache.computeIfAbsent(clientId + ":general", k -> Bucket.builder()
                    .addLimit(GENERAL_LIMIT)
                    .build());
        }

        if (!bucket.tryConsume(1)) {
            requestContext.abortWith(
                Response.status(429)
                    .entity("Too many requests. Please try again later.")
                    .header("Retry-After", "60")
                    .build()
            );
        }
    }

    private String getClientIdentifier(ContainerRequestContext requestContext) {
        // Try to use authenticated user ID first
        if (requestContext.getSecurityContext() != null && 
            requestContext.getSecurityContext().getUserPrincipal() != null) {
            return "user:" + requestContext.getSecurityContext().getUserPrincipal().getName();
        }
        
        // Fall back to IP address
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0];
        }
        
        return "ip:" + requestContext.getHeaderString("X-Forwarded-For");
    }
}
