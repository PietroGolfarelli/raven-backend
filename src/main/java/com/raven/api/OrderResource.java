package com.raven.api;

import com.raven.api.dto.Order;
import com.raven.api.repo.InMemoryStore;
import com.raven.api.repo.InMemoryStore.CreateOrderRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    InMemoryStore store;

    @Inject
    OrderBroadcaster broadcaster;

    @GET
    public Response list() {
        return Response.ok(store.list()).build();
    }

    @POST
    public Response create(CreateOrderRequest req) {
        Order created = store.create(req);
        broadcaster.broadcast(created);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    public static class UpdateStatusRequest {
        public String status; // "NEW" | "ACCEPTED" | "IN_PROGRESS" | "READY" | "COMPLETED" | "CANCELED"
    }

    @POST
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") String id, UpdateStatusRequest req) {
        if (req == null || req.status == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing status").build();
        }
        Order updated = store.updateStatus(id, req.status);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        broadcaster.broadcast(updated);
        return Response.ok(updated).build();
    }
}