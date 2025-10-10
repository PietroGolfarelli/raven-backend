package com.raven.api.dynamo;

import com.raven.api.dto.Order;

public class OrderMapper {
    public static OrderDdb toDdb(Order o) {
        OrderDdb d = new OrderDdb();
        d.setId(o.id);
        d.setStatus(o.status);
        d.setSource(o.source);
        d.setChannel(o.channel);
        d.setEtaMinutes(o.etaMinutes);
        d.setCustomer(o.customer);
        d.setNotes(o.notes);
        d.setItems(o.items);
        d.setCreatedAt(o.createdAt);
        d.setUpdatedAt(o.updatedAt);
        return d;
    }

    public static Order toApi(OrderDdb d) {
        Order o = new Order();
        o.id = d.getId();
        o.status = d.getStatus();
        o.source = d.getSource();
        o.channel = d.getChannel();
        o.etaMinutes = d.getEtaMinutes();
        o.customer = d.getCustomer();
        o.notes = d.getNotes();
        o.items = d.getItems();
        o.createdAt = d.getCreatedAt();
        o.updatedAt = d.getUpdatedAt();
        return o;
    }
}