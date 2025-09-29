// src/main/java/com/raven/api/OrderStore.java
package com.raven.api;

import com.raven.api.dto.Order;
import com.raven.api.repo.InMemoryStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class OrderStore {
    private final Map<String, Order> db = new ConcurrentHashMap<>();
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public List<Order> list() {
        return new ArrayList<>(db.values());
    }

    public Order create(InMemoryStore.CreateOrderRequest req) {
        Order o = new Order();
        o.id = UUID.randomUUID().toString();
        o.status = "NEW";
        o.channel = "counter";
        o.source = "mobile";
        o.createdAt = OffsetDateTime.now().format(ISO);
        o.updatedAt = o.createdAt;
        o.items = Collections.emptyList();
        db.put(o.id, o);
        return o;
    }

    public Order updateStatus(String id, String status) {
        Order o = db.get(id);
        if (o == null) return null;
        o.status = status;
        o.updatedAt = OffsetDateTime.now().format(ISO);
        return o;
    }
}