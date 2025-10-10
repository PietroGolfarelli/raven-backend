package com.raven.api;

import com.raven.api.repo.InMemoryStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    com.raven.api.repo.CatalogRepository catalog;

    @GET
    public java.util.List<com.raven.api.repo.InMemoryStore.Category> list() {
        return catalog.listCategories();
    }
}