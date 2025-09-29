package com.raven.api;

import com.raven.api.dto.Order;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/orders/stream")
public class OrderStreamResource {

    @Inject
    OrderBroadcaster broadcaster;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Order> stream() {
        // Invia default SSE events: il browser li riceve su es.onmessage
        return broadcaster.stream();
    }
}