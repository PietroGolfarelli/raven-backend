package com.raven.api.repo;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TableNames {
    @ConfigProperty(name = "raven.dynamo.table.categories")
    String categories;
    @ConfigProperty(name = "raven.dynamo.table.products")
    String products;
    @ConfigProperty(name = "raven.dynamo.table.orders")
    String orders;

    public String categories() {
        return categories;
    }

    public String products() {
        return products;
    }

    public String orders() {
        return orders;
    }
}