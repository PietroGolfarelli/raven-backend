package com.raven.api;

import com.raven.api.repo.InMemoryStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    InMemoryStore store;

    @GET
    public List<InMemoryStore.Product> list() {
        return store.getProducts();
    }
}