package com.raven.api;

import com.raven.api.dto.Order;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@ApplicationScoped
public class OrderBroadcaster {
    private final ConcurrentLinkedQueue<Consumer<Order>> subscribers = new ConcurrentLinkedQueue<>();

    public Multi<Order> stream() {
        return Multi.createFrom().<Order>emitter(emitter -> {
            Consumer<Order> consumer = emitter::emit;
            subscribers.add(consumer);
            emitter.onTermination(() -> subscribers.remove(consumer)); // Runnable richiesto
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    public void broadcast(Order order) {
        for (Consumer<Order> s : subscribers) {
            try {
                s.accept(order);
            } catch (Throwable ignored) {
            }
        }
    }

    public int subscriberCount() {
        return subscribers.size();
    }
}