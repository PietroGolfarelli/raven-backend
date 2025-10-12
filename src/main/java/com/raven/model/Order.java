
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Order domain model
 */
public class Order {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("status")
    private String status; // NEW, ACCEPTED, IN_PROGRESS, READY, COMPLETED, CANCELED
    
    @JsonProperty("source")
    private String source; // pos, mobile, restaurant_fe
    
    @JsonProperty("channel")
    private String channel; // counter, takeaway
    
    @JsonProperty("etaMinutes")
    private Integer etaMinutes;
    
    @JsonProperty("customer")
    private Customer customer;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("items")
    private List<OrderItem> items;
    
    @JsonProperty("createdAt")
    private String createdAt; // ISO date format
    
    @JsonProperty("updatedAt")
    private String updatedAt; // ISO date format
    
    // Constructors
    public Order() {
    }
    
    public Order(String id, String status, String source, String channel, Integer etaMinutes,
                 Customer customer, String notes, List<OrderItem> items, String createdAt, String updatedAt) {
        this.id = id;
        this.status = status;
        this.source = source;
        this.channel = channel;
        this.etaMinutes = etaMinutes;
        this.customer = customer;
        this.notes = notes;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public Integer getEtaMinutes() {
        return etaMinutes;
    }
    
    public void setEtaMinutes(Integer etaMinutes) {
        this.etaMinutes = etaMinutes;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", source='" + source + '\'' +
                ", channel='" + channel + '\'' +
                ", etaMinutes=" + etaMinutes +
                ", customer=" + customer +
                ", notes='" + notes + '\'' +
                ", items=" + items +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
