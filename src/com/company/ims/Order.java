package com.company.ims;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private final long id;
    private final String customerUsername;
    private final Instant createdAt;
    private OrderStatus status;
    private final List<OrderItem> items;

    public Order(long id, String customerUsername, List<OrderItem> items) {
        if (customerUsername == null || customerUsername.isBlank()) throw new IllegalArgumentException("Customer required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Items required");

        this.id = id;
        this.customerUsername = customerUsername.trim();
        this.items = new ArrayList<>(items);
        this.createdAt = Instant.now();
        this.status = OrderStatus.PLACED;
    }

    public long getId() { return id; }
    public String getCustomerUsername() { return customerUsername; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }

    public BigDecimal total() {
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem it : items) sum = sum.add(it.lineTotal());
        return sum;
    }

    public boolean isTerminal() {
        return status == OrderStatus.CANCELLED || status == OrderStatus.DELIVERED;
    }

    public void setStatus(OrderStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("Status required");
        if (isTerminal()) throw new IllegalStateException("Order is already finished");

        if (status == OrderStatus.PLACED) {
            if (newStatus != OrderStatus.PAID && newStatus != OrderStatus.CANCELLED)
                throw new IllegalArgumentException("From PLACED -> PAID or CANCELLED only");
        } else if (status == OrderStatus.PAID) {
            if (newStatus != OrderStatus.PACKED && newStatus != OrderStatus.CANCELLED)
                throw new IllegalArgumentException("From PAID -> PACKED or CANCELLED only");
        } else if (status == OrderStatus.PACKED) {
            if (newStatus != OrderStatus.SHIPPED)
                throw new IllegalArgumentException("From PACKED -> SHIPPED only");
        } else if (status == OrderStatus.SHIPPED) {
            if (newStatus != OrderStatus.DELIVERED)
                throw new IllegalArgumentException("From SHIPPED -> DELIVERED only");
        }

        this.status = newStatus;
    }

    public boolean canCustomerCancel() {
        return status == OrderStatus.PLACED || status == OrderStatus.PAID;
    }
}
