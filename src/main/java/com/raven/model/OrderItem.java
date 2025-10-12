
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OrderItem nested object for Order
 */
public class OrderItem {
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("notes")
    private String notes;
    
    // Constructors
    public OrderItem() {
    }
    
    public OrderItem(String productId, String productName, Integer quantity, Double price, String notes) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", notes='" + notes + '\'' +
                '}';
    }
}
