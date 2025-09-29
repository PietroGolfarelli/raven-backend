package com.raven.api.dto;

public class Product {
    public String id;
    public String name;
    public double price;
    public String categoryId;
    public boolean visible;

    public Product() {
    }

    public Product(String id, String name, double price, String categoryId, boolean visible) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.visible = visible;
    }
}