package com.packtrace.resource;

import com.packtrace.dto.GearRequest;
import com.packtrace.dto.GearResponse;
import com.packtrace.model.Gear;
import com.packtrace.mapper.GearMapper;
import com.packtrace.service.GearService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/gear")
@Authenticated
public class GearResource {

    @Inject
    GearService gearService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<GearResponse> getMyGear() {
        String auth0Id = jwt.getSubject();
        return gearService.getMyGear(auth0Id)
                .stream()
                .map(GearMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGearById(@PathParam("id") Long id) {
        String auth0Id = jwt.getSubject();
        try {
            return gearService.getGearById(id)
                    .map(gear -> Response.ok(GearMapper.toResponse(gear)).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGear(@Valid GearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Gear gear = GearMapper.toEntity(request);
            Gear createdGear = gearService.createGear(auth0Id, gear);
            return Response.status(Response.Status.CREATED)
                    .entity(GearMapper.toResponse(createdGear))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGear(@PathParam("id") Long id, @Valid GearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Gear gear = GearMapper.toEntity(request);
            Gear updatedGear = gearService.updateGear(id, gear, auth0Id);
            return Response.ok(GearMapper.toResponse(updatedGear)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGear(@PathParam("id") Long id) {
        String auth0Id = jwt.getSubject();
        try {
            gearService.deleteGear(id, auth0Id);
            return Response.noContent().build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    public record ErrorResponse(String message) {}
}
