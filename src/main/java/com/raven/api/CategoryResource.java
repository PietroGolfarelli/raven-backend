
package com.raven.api;

import com.raven.model.Category;
import com.raven.repository.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST API for Category management
 */
@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryResource {
    
    private static final Logger LOG = Logger.getLogger(CategoryResource.class);
    
    @Inject
    CategoryRepository categoryRepository;
    
    @GET
    @Operation(summary = "Get all categories", description = "Retrieve all categories from the database")
    public Response getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findAll();
            return Response.ok(categories).build();
        } catch (Exception e) {
            LOG.error("Error getting all categories", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve categories"))
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a single category by its ID")
    public Response getCategoryById(@PathParam("id") String id) {
        try {
            return categoryRepository.findById(id)
                .map(category -> Response.ok(category).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Category not found with ID: " + id))
                    .build());
        } catch (Exception e) {
            LOG.errorf(e, "Error getting category by ID: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve category"))
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Create category", description = "Create a new category")
    public Response createCategory(Category category) {
        try {
            if (category == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Category data is required"))
                    .build();
            }
            
            if (category.getName() == null || category.getName().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Category name is required"))
                    .build();
            }
            
            Category created = categoryRepository.create(category);
            return Response.status(Response.Status.CREATED).entity(created).build();
            
        } catch (Exception e) {
            LOG.error("Error creating category", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to create category"))
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public Response updateCategory(@PathParam("id") String id, Category category) {
        try {
            if (category == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Category data is required"))
                    .build();
            }
            
            if (category.getName() == null || category.getName().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Category name is required"))
                    .build();
            }
            
            Category updated = categoryRepository.update(id, category);
            return Response.ok(updated).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error updating category: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to update category"))
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category by ID")
    public Response deleteCategory(@PathParam("id") String id) {
        try {
            boolean deleted = categoryRepository.delete(id);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Category not found with ID: " + id))
                    .build();
            }
            
        } catch (Exception e) {
            LOG.errorf(e, "Error deleting category: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to delete category"))
                .build();
        }
    }
    
    // Error response class
    public static class ErrorResponse {
        public String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
