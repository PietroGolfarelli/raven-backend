
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Category domain model
 */
public class Category {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("color")
    private String color;
    
    @JsonProperty("sortOrder")
    private Integer sortOrder;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("icon")
    private String icon;
    
    // Constructors
    public Category() {
    }
    
    public Category(String id, String name, String color, Integer sortOrder, String description, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.sortOrder = sortOrder;
        this.description = description;
        this.icon = icon;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", sortOrder=" + sortOrder +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
