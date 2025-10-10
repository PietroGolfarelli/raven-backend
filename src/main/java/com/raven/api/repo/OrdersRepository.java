package com.raven.api.repo;

import com.raven.api.dto.Order;

import java.util.List;

public interface OrdersRepository {
    List<Order> list();

    Order create(InMemoryStore.CreateOrderRequest req);

    Order updateStatus(String id, String status);
}