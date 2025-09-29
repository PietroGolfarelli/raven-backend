package com.raven.api.dto;

public class Category {
    public String id;
    public String name;
    public boolean visible;

    public Category() {
    }

    public Category(String id, String name, boolean visible) {
        this.id = id;
        this.name = name;
        this.visible = visible;
    }
}