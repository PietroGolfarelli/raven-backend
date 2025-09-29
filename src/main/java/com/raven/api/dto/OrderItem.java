package com.raven.api.dto;

public class OrderItem {
    public String productId;
    public int qty;

    public OrderItem() {
    }

    public OrderItem(String productId, int qty) {
        this.productId = productId;
        this.qty = qty;
    }
}