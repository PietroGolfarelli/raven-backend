
package com.raven.api;

import com.raven.model.Product;
import com.raven.repository.ProductRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST API for Product management
 */
@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "Product management endpoints")
public class ProductResource {
    
    private static final Logger LOG = Logger.getLogger(ProductResource.class);
    
    @Inject
    ProductRepository productRepository;
    
    @GET
    @Operation(summary = "Get all products", description = "Retrieve all products from the database")
    public Response getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return Response.ok(products).build();
        } catch (Exception e) {
            LOG.error("Error getting all products", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve products"))
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its ID")
    public Response getProductById(@PathParam("id") String id) {
        try {
            return productRepository.findById(id)
                .map(product -> Response.ok(product).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Product not found with ID: " + id))
                    .build());
        } catch (Exception e) {
            LOG.errorf(e, "Error getting product by ID: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve product"))
                .build();
        }
    }
    
    @GET
    @Path("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve all products for a specific category")
    public Response getProductsByCategory(@PathParam("categoryId") String categoryId) {
        try {
            List<Product> products = productRepository.findByCategoryId(categoryId);
            return Response.ok(products).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error getting products by category: %s", categoryId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve products by category"))
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Create product", description = "Create a new product")
    public Response createProduct(Product product) {
        try {
            if (product == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product data is required"))
                    .build();
            }
            
            if (product.getName() == null || product.getName().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product name is required"))
                    .build();
            }
            
            if (product.getCategoryId() == null || product.getCategoryId().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product categoryId is required"))
                    .build();
            }
            
            Product created = productRepository.create(product);
            return Response.status(Response.Status.CREATED).entity(created).build();
            
        } catch (Exception e) {
            LOG.error("Error creating product", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to create product"))
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public Response updateProduct(@PathParam("id") String id, Product product) {
        try {
            if (product == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product data is required"))
                    .build();
            }
            
            if (product.getName() == null || product.getName().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product name is required"))
                    .build();
            }
            
            if (product.getCategoryId() == null || product.getCategoryId().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Product categoryId is required"))
                    .build();
            }
            
            Product updated = productRepository.update(id, product);
            return Response.ok(updated).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error updating product: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to update product"))
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product by ID")
    public Response deleteProduct(@PathParam("id") String id) {
        try {
            boolean deleted = productRepository.delete(id);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Product not found with ID: " + id))
                    .build();
            }
            
        } catch (Exception e) {
            LOG.errorf(e, "Error deleting product: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to delete product"))
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
