package com.raven.api.dto;

import java.util.List;

public class Order {
    public String id;
    // "NEW" | "ACCEPTED" | "IN_PROGRESS" | "READY" | "COMPLETED" | "CANCELED"
    public String status;
    // "pos" | "mobile" | "restaurant_fe" ecc.
    public String source;
    // "counter" | "takeaway"
    public String channel;

    public Integer etaMinutes;
    public Customer customer;
    public String notes;
    public List<OrderItem> items;

    // Date come ISO stringhe per semplicità JSON-B
    public String createdAt;
    public String updatedAt;

    public static class Customer {
        public String name;
        public String phone;
    }

    public static class OrderItem {
        public String productId;
        public String nameSnapshot;
        public Integer qty;
        public Double unitPrice;
        public List<Modifier> modifiers;
        public String notes;
        public String station; // "kitchen" | "bar"
        public String course;  // "starter" | "main" | "dessert"
    }

    public static class Modifier {
        public String id;        // opzionale (il FE può generarlo se mancante)
        public String name;
        public Double priceDelta;
    }
}