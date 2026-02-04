package com.packtrace.resource;

import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;

@Path("/health")
public class HealthResource {

    @Inject
    AgroalDataSource dataSource;

    @GET
    @Path("/db")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            if (isValid) {
                return Response.ok()
                    .entity(new HealthResponse("Database connection OK", true))
                    .build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(new HealthResponse("Database connection failed", false))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthResponse("Database error: " + e.getMessage(), false))
                .build();
        }
    }

    public record HealthResponse(String message, boolean healthy) {}
}
