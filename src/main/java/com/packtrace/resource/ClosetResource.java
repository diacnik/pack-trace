package com.packtrace.resource;

import com.packtrace.dto.ClosetGearRequest;
import com.packtrace.dto.ClosetGearResponse;
import com.packtrace.dto.ClosetRequest;
import com.packtrace.dto.ClosetResponse;
import com.packtrace.model.Closet;
import com.packtrace.mapper.ClosetMapper;
import com.packtrace.service.ClosetService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/closet")
@Authenticated
public class ClosetResource {

    @Inject
    ClosetService closetService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClosetResponse> getMyClosets() {
        String auth0Id = jwt.getSubject();
        return closetService.getMyClosets(auth0Id)
                .stream()
                .map(ClosetMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClosetById(@PathParam("id") Long id) {
        return closetService.getClosetById(id)
                .map(closet -> Response.ok(ClosetMapper.toResponse(closet)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCloset(@Valid ClosetRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Closet closet = ClosetMapper.toEntity(request);
            Closet createdCloset = closetService.createCloset(auth0Id, closet);
            return Response.status(Response.Status.CREATED)
                    .entity(ClosetMapper.toResponse(createdCloset))
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
    public Response updateCloset(@PathParam("id") Long id, @Valid ClosetRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Closet closet = ClosetMapper.toEntity(request);
            Closet updatedCloset = closetService.updateCloset(id, closet, auth0Id);
            return Response.ok(ClosetMapper.toResponse(updatedCloset)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCloset(@PathParam("id") Long id) {
        String auth0Id = jwt.getSubject();
        try {
            closetService.deleteCloset(id, auth0Id);
            return Response.noContent().build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}/gear")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClosetGearResponse> getClosetGear(@PathParam("id") Long closetId) {
        return closetService.getClosetGear(closetId);
    }

    @POST
    @Path("/{id}/gear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGearToCloset(@PathParam("id") Long closetId, @Valid ClosetGearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            closetService.addGearToCloset(closetId, request.gearId(), request.quantity(), auth0Id);
            return Response.status(Response.Status.CREATED).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}/gear/{gearId}")
    public Response removeGearFromCloset(@PathParam("id") Long closetId, @PathParam("gearId") Long gearId) {
        String auth0Id = jwt.getSubject();
        try {
            closetService.removeGearFromCloset(closetId, gearId, auth0Id);
            return Response.noContent().build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/gear/{gearId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateClosetGearQuantity(@PathParam("id") Long closetId, @PathParam("gearId") Long gearId, 
                                             @Valid ClosetGearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            closetService.updateClosetGearQuantity(closetId, gearId, request.quantity(), auth0Id);
            return Response.ok().build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    public record ErrorResponse(String message) {}
}
