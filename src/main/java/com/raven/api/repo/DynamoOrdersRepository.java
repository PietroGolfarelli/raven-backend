package com.raven.api.repo;

import com.raven.api.dto.Order;
import com.raven.api.dynamo.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class DynamoOrdersRepository implements OrdersRepository {

    private final DynamoDbTable<OrderDdb> orderTable;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Inject
    public DynamoOrdersRepository(DynamoDbEnhancedClient enhanced, TableNames names) {
        this.orderTable = enhanced.table(names.orders(), TableSchema.fromBean(OrderDdb.class));
    }

    @Override
    public List<Order> list() {
        var scan = orderTable.scan();
        return StreamSupport.stream(scan.items().spliterator(), false)
                .map(OrderMapper::toApi)
                .toList();
    }

    @Override
    public Order create(InMemoryStore.CreateOrderRequest req) {
        Order o = new Order();
        o.id = UUID.randomUUID().toString();
        o.status = "NEW";
        o.channel = (req.channel != null ? req.channel : "counter");
        o.source = (req.source != null ? req.source : "mobile");

        String now = OffsetDateTime.now().format(ISO);
        o.createdAt = now;
        o.updatedAt = now;

        o.notes = req.notes;
        o.etaMinutes = req.etaMinutes;

        if (req.customer != null) {
            Order.Customer c = new Order.Customer();
            c.name = req.customer.name;
            c.phone = req.customer.phone;
            o.customer = c;
        }

        o.items = new ArrayList<>();
        if (req.items != null) {
            for (InMemoryStore.CreateOrderItem ci : req.items) {
                Order.OrderItem it = new Order.OrderItem();
                it.productId = ci.productId;
                it.nameSnapshot = (ci.nameSnapshot != null ? ci.nameSnapshot : ci.productId);
                it.qty = (ci.qty != null ? ci.qty : 1);
                it.unitPrice = (ci.unitPrice != null ? ci.unitPrice : 0.0);
                it.notes = ci.notes;
                it.station = ci.station;
                it.course = ci.course;

                if (ci.modifiers != null) {
                    it.modifiers = new ArrayList<>();
                    for (InMemoryStore.CreateOrderModifier cm : ci.modifiers) {
                        Order.Modifier m = new Order.Modifier();
                        m.id = cm.id;
                        m.name = cm.name;
                        m.priceDelta = (cm.priceDelta != null ? cm.priceDelta : 0.0);
                        it.modifiers.add(m);
                    }
                }
                o.items.add(it);
            }
        }

        orderTable.putItem(OrderMapper.toDdb(o));
        return o;
    }

    @Override
    public Order updateStatus(String id, String status) {
        var key = Key.builder().partitionValue(id).build();
        OrderDdb existing = orderTable.getItem(key);
        if (existing == null) return null;
        existing.setStatus(status);
        existing.setUpdatedAt(OffsetDateTime.now().format(ISO));
        orderTable.updateItem(existing);
        return OrderMapper.toApi(existing);
    }
}