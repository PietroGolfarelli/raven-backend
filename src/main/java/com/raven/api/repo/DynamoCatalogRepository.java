package com.raven.api.repo;

import com.raven.api.dynamo.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class DynamoCatalogRepository implements CatalogRepository {

    private final DynamoDbTable<CategoryDdb> categoryTable;
    private final DynamoDbTable<ProductDdb> productTable;

    @Inject
    public DynamoCatalogRepository(DynamoDbEnhancedClient enhanced, TableNames names) {
        this.categoryTable = enhanced.table(names.categories(), TableSchema.fromBean(CategoryDdb.class));
        this.productTable = enhanced.table(names.products(), TableSchema.fromBean(ProductDdb.class));
    }

    @Override
    public List<InMemoryStore.Category> listCategories() {
        var results = categoryTable.scan();
        return StreamSupport.stream(results.items().spliterator(), false)
                .map(CatalogMapper::toApi)
                .sorted(Comparator.comparing(c -> c.sortOrder == null ? Integer.MAX_VALUE : c.sortOrder))
                .toList();
    }

    @Override
    public List<InMemoryStore.Product> listProducts() {
        var results = productTable.scan();
        return StreamSupport.stream(results.items().spliterator(), false)
                .map(CatalogMapper::toApi)
                .toList();
    }
}