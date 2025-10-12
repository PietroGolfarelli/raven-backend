
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Product domain model
 */
public class Product {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("categoryId")
    private String categoryId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("price")
    private Double price;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @JsonProperty("taxRateId")
    private String taxRateId;
    
    @JsonProperty("visibleOn")
    private VisibleOn visibleOn;
    
    @JsonProperty("ingredients")
    private List<String> ingredients;
    
    @JsonProperty("allergens")
    private List<String> allergens;
    
    // Constructors
    public Product() {
    }
    
    public Product(String id, String categoryId, String name, String description, Double price, 
                   String imageUrl, String taxRateId, VisibleOn visibleOn, 
                   List<String> ingredients, List<String> allergens) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.taxRateId = taxRateId;
        this.visibleOn = visibleOn;
        this.ingredients = ingredients;
        this.allergens = allergens;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getTaxRateId() {
        return taxRateId;
    }
    
    public void setTaxRateId(String taxRateId) {
        this.taxRateId = taxRateId;
    }
    
    public VisibleOn getVisibleOn() {
        return visibleOn;
    }
    
    public void setVisibleOn(VisibleOn visibleOn) {
        this.visibleOn = visibleOn;
    }
    
    public List<String> getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
    
    public List<String> getAllergens() {
        return allergens;
    }
    
    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imageUrl='" + imageUrl + '\'' +
                ", taxRateId='" + taxRateId + '\'' +
                ", visibleOn=" + visibleOn +
                ", ingredients=" + ingredients +
                ", allergens=" + allergens +
                '}';
    }
}
