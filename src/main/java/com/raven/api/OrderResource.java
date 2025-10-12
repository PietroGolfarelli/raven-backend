
package com.raven.api;

import com.raven.model.Order;
import com.raven.repository.OrderRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

/**
 * REST API for Order management
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderResource {
    
    private static final Logger LOG = Logger.getLogger(OrderResource.class);
    
    @Inject
    OrderRepository orderRepository;
    
    @GET
    @Operation(summary = "Get all orders", description = "Retrieve all orders from the database")
    public Response getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();
            return Response.ok(orders).build();
        } catch (Exception e) {
            LOG.error("Error getting all orders", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve orders"))
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a single order by its ID")
    public Response getOrderById(@PathParam("id") String id) {
        try {
            return orderRepository.findById(id)
                .map(order -> Response.ok(order).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Order not found with ID: " + id))
                    .build());
        } catch (Exception e) {
            LOG.errorf(e, "Error getting order by ID: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to retrieve order"))
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Create order", description = "Create a new order")
    public Response createOrder(Order order) {
        try {
            if (order == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order data is required"))
                    .build();
            }
            
            if (order.getStatus() == null || order.getStatus().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order status is required"))
                    .build();
            }
            
            if (order.getItems() == null || order.getItems().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order must have at least one item"))
                    .build();
            }
            
            Order created = orderRepository.create(order);
            return Response.status(Response.Status.CREATED).entity(created).build();
            
        } catch (Exception e) {
            LOG.error("Error creating order", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to create order"))
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update order", description = "Update an existing order")
    public Response updateOrder(@PathParam("id") String id, Order order) {
        try {
            if (order == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order data is required"))
                    .build();
            }
            
            if (order.getStatus() == null || order.getStatus().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order status is required"))
                    .build();
            }
            
            if (order.getItems() == null || order.getItems().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Order must have at least one item"))
                    .build();
            }
            
            Order updated = orderRepository.update(id, order);
            return Response.ok(updated).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error updating order: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to update order"))
                .build();
        }
    }
    
    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Update order status", description = "Update only the status of an existing order")
    public Response updateOrderStatus(@PathParam("id") String id, Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            
            if (newStatus == null || newStatus.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Status is required"))
                    .build();
            }
            
            Order updated = orderRepository.updateStatus(id, newStatus);
            return Response.ok(updated).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.errorf(e, "Error updating order status: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to update order status"))
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete order", description = "Delete an order by ID")
    public Response deleteOrder(@PathParam("id") String id) {
        try {
            boolean deleted = orderRepository.delete(id);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Order not found with ID: " + id))
                    .build();
            }
            
        } catch (Exception e) {
            LOG.errorf(e, "Error deleting order: %s", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Failed to delete order"))
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
