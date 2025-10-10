package com.raven.api.dynamo;

import com.raven.api.repo.InMemoryStore;

import java.util.ArrayList;

public final class CatalogMapper {
    private CatalogMapper() {}

    public static InMemoryStore.Product toApi(ProductDdb d) {
        InMemoryStore.Product p = new InMemoryStore.Product();
        p.id = d.getId();
        p.categoryId = d.getCategoryId();
        p.name = d.getName();
        p.description = d.getDescription();
        p.price = d.getPrice();
        p.imageUrl = d.getImageUrl();
        p.taxRateId = d.getTaxRateId();

        // visibleOn
        if (d.getVisibleOn() != null) {
            InMemoryStore.Product.VisibleOn vo = new InMemoryStore.Product.VisibleOn();
            vo.pos = d.getVisibleOn().isPos();
            vo.app = d.getVisibleOn().isApp();
            p.visibleOn = vo;
        }

        p.ingredients = d.getIngredients() != null ? new ArrayList<>(d.getIngredients()) : new ArrayList<>();
        p.allergens = d.getAllergens() != null ? new ArrayList<>(d.getAllergens()) : new ArrayList<>();
        return p;
    }

    public static InMemoryStore.Category toApi(CategoryDdb d) {
        InMemoryStore.Category c = new InMemoryStore.Category();
        c.id = d.getId();
        c.name = d.getName();
        c.color = d.getColor();
        c.sortOrder = d.getSortOrder();
        c.description = d.getDescription();
        c.icon = d.getIcon();
        return c;
    }
}