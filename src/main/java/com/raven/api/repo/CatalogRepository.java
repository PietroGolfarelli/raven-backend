package com.raven.api.repo;

import java.util.List;

public interface CatalogRepository {
    List<InMemoryStore.Category> listCategories();

    List<InMemoryStore.Product> listProducts();
}