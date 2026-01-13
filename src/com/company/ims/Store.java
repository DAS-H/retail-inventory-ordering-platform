package com.company.ims;

import java.math.BigDecimal;
import java.util.*;

public class Store {
    private final Map<String, User> usersByName = new HashMap<>();
    private final Map<String, Product> productsBySku = new LinkedHashMap<>();
    private final Map<Long, Order> ordersById = new LinkedHashMap<>();
    private long nextOrderId = 1000;

    public Store() {
        usersByName.put("admin", new User("admin", "admin123", Role.ADMIN));
        usersByName.put("Omkar", new User("Omkar", "user123", Role.USER));

        addProduct(new Product("SKU-100", "Notebook", new BigDecimal("3.49"), 40, 10));
        addProduct(new Product("SKU-200", "Pen", new BigDecimal("1.25"), 75, 15));
        addProduct(new Product("SKU-300", "Backpack", new BigDecimal("29.99"), 8, 8));
    }

    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        User u = usersByName.get(username.trim());
        if (u == null) return Optional.empty();
        return u.checkPassword(password) ? Optional.of(u) : Optional.empty();
    }

    public Collection<Product> listProducts() {
        return new ArrayList<>(productsBySku.values());
    }

    public Optional<Product> findProduct(String sku) {
        if (sku == null) return Optional.empty();
        return Optional.ofNullable(productsBySku.get(sku.trim()));
    }

    public void addProduct(Product p) {
        if (p == null) throw new IllegalArgumentException("Product required");
        if (productsBySku.containsKey(p.getSku())) throw new IllegalArgumentException("SKU already exists");
        productsBySku.put(p.getSku(), p);
    }

    public void removeProduct(String sku) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU required");
        Product removed = productsBySku.remove(sku.trim());
        if (removed == null) throw new IllegalArgumentException("SKU not found");
    }

    public List<Product> lowStockProducts() {
        List<Product> out = new ArrayList<>();
        for (Product p : productsBySku.values()) {
            if (p.isLowStock()) out.add(p);
        }
        return out;
    }

    public Order placeOrder(String username, Map<String, Integer> skuToQty) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("User required");
        if (skuToQty == null || skuToQty.isEmpty()) throw new IllegalArgumentException("Order items required");

        List<OrderItem> items = new ArrayList<>();

        for (Map.Entry<String, Integer> e : skuToQty.entrySet()) {
            String sku = e.getKey();
            Integer qty = e.getValue();
            if (sku == null || sku.isBlank()) throw new IllegalArgumentException("Bad SKU in cart");
            if (qty == null || qty <= 0) throw new IllegalArgumentException("Quantity must be > 0");

            Product p = productsBySku.get(sku.trim());
            if (p == null) throw new IllegalArgumentException("SKU not found: " + sku);
            if (qty > p.getStock()) throw new IllegalArgumentException("Not enough stock for " + sku + " (have " + p.getStock() + ")");

            items.add(new OrderItem(p.getSku(), p.getName(), p.getPrice(), qty));
        }

        for (OrderItem it : items) {
            Product p = productsBySku.get(it.getSku());
            p.removeStock(it.getQuantity());
        }

        long id = nextOrderId++;
        Order o = new Order(id, username, items);
        ordersById.put(id, o);
        return o;
    }

    public Optional<Order> findOrder(long id) {
        return Optional.ofNullable(ordersById.get(id));
    }

    public List<Order> listOrdersForUser(String username) {
        List<Order> out = new ArrayList<>();
        for (Order o : ordersById.values()) {
            if (o.getCustomerUsername().equals(username)) out.add(o);
        }
        return out;
    }

    public List<Order> listAllOrders() {
        return new ArrayList<>(ordersById.values());
    }

    public void cancelOrderAsCustomer(String username, long orderId) {
        Order o = ordersById.get(orderId);
        if (o == null) throw new IllegalArgumentException("Order not found");
        if (!o.getCustomerUsername().equals(username)) throw new IllegalArgumentException("Not your order");
        if (!o.canCustomerCancel()) throw new IllegalArgumentException("Cannot cancel at status: " + o.getStatus());

        restockFromOrder(o);
        o.setStatus(OrderStatus.CANCELLED);
    }

    public void updateOrderStatusAsAdmin(long orderId, OrderStatus newStatus) {
        Order o = ordersById.get(orderId);
        if (o == null) throw new IllegalArgumentException("Order not found");

        if (newStatus == OrderStatus.CANCELLED) {
            if (o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)
                throw new IllegalArgumentException("Cannot cancel shipped/delivered order");
            restockFromOrder(o);
        }

        o.setStatus(newStatus);
    }

    private void restockFromOrder(Order o) {
        for (OrderItem it : o.getItems()) {
            Product p = productsBySku.get(it.getSku());
            if (p != null) p.addStock(it.getQuantity());
        }
    }
}
