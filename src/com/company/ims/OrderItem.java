package com.company.ims;

import java.math.BigDecimal;

public class OrderItem {
    private final String sku;
    private final String name;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderItem(String sku, String name, BigDecimal unitPrice, int quantity) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (unitPrice == null || unitPrice.signum() < 0) throw new IllegalArgumentException("Price must be >= 0");
        if (quantity <= 0) throw new IllegalArgumentException("Qty must be > 0");

        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
