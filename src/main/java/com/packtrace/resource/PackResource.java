package com.packtrace.resource;

import com.packtrace.dto.PackGearRequest;
import com.packtrace.dto.PackGearResponse;
import com.packtrace.dto.PackRequest;
import com.packtrace.dto.PackResponse;
import com.packtrace.dto.CreateAndAddGearToPackRequest;
import com.packtrace.model.Pack;
import com.packtrace.model.Gear;
import com.packtrace.mapper.PackMapper;
import com.packtrace.mapper.GearMapper;
import com.packtrace.service.PackService;
import com.packtrace.validation.PositiveId;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/pack")
@Authenticated
public class PackResource {

    @Inject
    PackService packService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PackResponse> getMyPacks() {
        String auth0Id = jwt.getSubject();
        return packService.getMyPacks(auth0Id)
                .stream()
                .map(PackMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPackById(@PathParam("id") @PositiveId Long id) {
        String auth0Id = jwt.getSubject();
        try {
            return packService.getPackById(id, auth0Id)
                    .map(pack -> Response.ok(PackMapper.toResponse(pack)).build())
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
    public Response createPack(@Valid PackRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Pack pack = PackMapper.toEntity(request);
            Pack createdPack = packService.createPack(auth0Id, pack);
            return Response.status(Response.Status.CREATED)
                    .entity(PackMapper.toResponse(createdPack))
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
    public Response updatePack(@PathParam("id") @PositiveId Long id, @Valid PackRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Pack pack = PackMapper.toEntity(request);
            Pack updatedPack = packService.updatePack(id, pack, auth0Id);
            return Response.ok(PackMapper.toResponse(updatedPack)).build();
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
    public Response deletePack(@PathParam("id") @PositiveId Long id) {
        String auth0Id = jwt.getSubject();
        try {
            packService.deletePack(id, auth0Id);
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
    public List<PackGearResponse> getPackGear(@PathParam("id") @PositiveId Long packId) {
        return packService.getPackGear(packId);
    }

    @POST
    @Path("/{id}/gear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGearToPack(@PathParam("id") @PositiveId Long packId, @Valid PackGearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            packService.addGearToPack(packId, request.gearId(), request.quantity(), auth0Id);
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

    @POST
    @Path("/{id}/gear/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGearAndAddToPack(@PathParam("id") @PositiveId Long packId, @Valid CreateAndAddGearToPackRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            Gear gear = new Gear();
            gear.setName(request.name());
            gear.setBrand(request.brand());
            gear.setWeightGrams(request.weightGrams());
            gear.setWebsiteURL(request.websiteURL());
            gear.setCategory(request.category());

            Gear createdGear = packService.createGearAndAddToPack(packId, gear, request.quantity(), auth0Id);
            return Response.status(Response.Status.CREATED)
                    .entity(GearMapper.toResponse(createdGear))
                    .build();
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
    public Response removeGearFromPack(@PathParam("id") @PositiveId Long packId, @PathParam("gearId") @PositiveId Long gearId) {
        String auth0Id = jwt.getSubject();
        try {
            packService.removeGearFromPack(packId, gearId, auth0Id);
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
    public Response updatePackGearQuantity(@PathParam("id") @PositiveId Long packId, @PathParam("gearId") @PositiveId Long gearId, 
                                           @Valid PackGearRequest request) {
        String auth0Id = jwt.getSubject();
        try {
            packService.updatePackGearQuantity(packId, gearId, request.quantity(), auth0Id);
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
