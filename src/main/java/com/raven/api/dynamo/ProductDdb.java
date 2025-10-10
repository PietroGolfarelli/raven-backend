package com.raven.api.dynamo;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.List;

@DynamoDbBean
public class ProductDdb {
    private String id;
    private String categoryId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String taxRateId;
    private VisibleOn visibleOn = new VisibleOn();
    private List<String> ingredients;
    private List<String> allergens;

    @DynamoDbBean
    public static class VisibleOn {
        private boolean pos = true;
        private boolean app = true;
        public boolean isPos() { return pos; }
        public void setPos(boolean pos) { this.pos = pos; }
        public boolean isApp() { return app; }
        public void setApp(boolean app) { this.app = app; }
    }

    @DynamoDbPartitionKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @DynamoDbSecondaryPartitionKey(indexNames = "products_by_category")
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTaxRateId() { return taxRateId; }
    public void setTaxRateId(String taxRateId) { this.taxRateId = taxRateId; }

    public VisibleOn getVisibleOn() { return visibleOn; }
    public void setVisibleOn(VisibleOn visibleOn) { this.visibleOn = visibleOn; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public List<String> getAllergens() { return allergens; }
    public void setAllergens(List<String> allergens) { this.allergens = allergens; }
}