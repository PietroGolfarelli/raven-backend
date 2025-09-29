package com.raven.api.repo;

import com.raven.api.dto.Order;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryStore {

    // ===== Orders =====
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // ===== Categories & Products (mock) =====
    // Strutture minime per far funzionare il FE (allinea ai tuoi DTO se già li hai)
    public static class Category {
        public String id;
        public String name;
        public String color;       // es.: "#10b981"
        public Integer sortOrder;  // per ordinamento
        public String description;
        public String icon;
    }

    public static class Product {
        public String id;
        public String categoryId;
        public String name;
        public String description;
        public Double price;
        public String imageUrl;
        public String taxRateId;   // opzionale per ora
        public VisibleOn visibleOn = new VisibleOn();

        public static class VisibleOn {
            public boolean pos = true;
            public boolean app = true;
        }
    }

    private final List<Category> categories = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();

    @PostConstruct
    void seed() {
        // Seed minimo per evitare liste vuote
        if (categories.isEmpty()) {
            Category c1 = new Category();
            c1.id = "cat-burger";
            c1.name = "Burger";
            c1.color = "#10b981";
            c1.sortOrder = 1;
            c1.description = "Burger e panini";
            c1.icon = "🍔";

            Category c2 = new Category();
            c2.id = "cat-drinks";
            c2.name = "Bevande";
            c2.color = "#3b82f6";
            c2.sortOrder = 2;
            c2.description = "Bibite e drink";
            c2.icon = "🥤";

            categories.add(c1);
            categories.add(c2);
        }
        if (products.isEmpty()) {
            Product p1 = new Product();
            p1.id = "prod-burger-classic";
            p1.categoryId = "cat-burger";
            p1.name = "Burger Classic";
            p1.description = "Manzo, insalata, pomodoro";
            p1.price = 8.50;
            p1.imageUrl = null;
            p1.taxRateId = "tax-std";

            Product p2 = new Product();
            p2.id = "prod-cola";
            p2.categoryId = "cat-drinks";
            p2.name = "Cola";
            p2.description = "Lattina 330ml";
            p2.price = 2.50;
            p2.imageUrl = null;
            p2.taxRateId = "tax-std";

            products.add(p1);
            products.add(p2);
        }
    }

    // ===== API Categories & Products =====
    public List<Category> getCategories() {
        // eventualmente ordina per sortOrder
        return new ArrayList<>(categories);
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    // ===== API Orders =====
    public List<Order> list() {
        return new ArrayList<>(orders.values());
    }

    public Order create(CreateOrderRequest req) {
        Order o = new Order();
        o.id = UUID.randomUUID().toString();
        o.status = "NEW";
        o.channel = req.channel != null ? req.channel : "counter";
        o.source = req.source != null ? req.source : "mobile";

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
            for (CreateOrderItem ci : req.items) {
                Order.OrderItem it = new Order.OrderItem();
                it.productId = ci.productId;
                it.nameSnapshot = ci.nameSnapshot != null ? ci.nameSnapshot : ci.productId;
                it.qty = ci.qty != null ? ci.qty : 1;
                it.unitPrice = ci.unitPrice != null ? ci.unitPrice : 0.0;
                it.notes = ci.notes;
                it.station = ci.station;
                it.course = ci.course;
                it.modifiers = new ArrayList<>();
                if (ci.modifiers != null) {
                    for (CreateOrderModifier cm : ci.modifiers) {
                        Order.Modifier m = new Order.Modifier();
                        m.id = cm.id; // opzionale
                        m.name = cm.name;
                        m.priceDelta = cm.priceDelta != null ? cm.priceDelta : 0.0;
                        it.modifiers.add(m);
                    }
                }
                o.items.add(it);
            }
        }

        orders.put(o.id, o);
        return o;
    }

    public Order updateStatus(String id, String status) {
        Order o = orders.get(id);
        if (o == null) return null;
        o.status = status;
        o.updatedAt = OffsetDateTime.now().format(ISO);
        return o;
    }

    // ===== DTO Create Order =====
    public static class CreateOrderRequest {
        public String source;  // "mobile" | "pos" | ...
        public String channel; // "counter" | "takeaway"
        public Integer etaMinutes;
        public Customer customer;
        public String notes;
        public List<CreateOrderItem> items;

        public static class Customer {
            public String name;
            public String phone;
        }
    }

    public static class CreateOrderItem {
        public String productId;
        public String nameSnapshot;
        public Integer qty;
        public Double unitPrice;
        public String notes;
        public String station; // "kitchen" | "bar"
        public String course;  // "starter" | "main" | "dessert"
        public List<CreateOrderModifier> modifiers;
    }

    public static class CreateOrderModifier {
        public String id;
        public String name;
        public Double priceDelta;
    }
}