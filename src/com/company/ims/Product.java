package com.company.ims;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private final String sku;
    private String name;
    private BigDecimal price;
    private int stock;
    private int lowStockThreshold;

    public Product(String sku, String name, BigDecimal price, int stock, int lowStockThreshold) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Price must be >= 0");
        if (stock < 0) throw new IllegalArgumentException("Stock must be >= 0");
        if (lowStockThreshold < 0) throw new IllegalArgumentException("Threshold must be >= 0");

        this.sku = sku.trim();
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public int getLowStockThreshold() { return lowStockThreshold; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        this.name = name.trim();
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Price must be >= 0");
        this.price = price;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold < 0) throw new IllegalArgumentException("Threshold must be >= 0");
        this.lowStockThreshold = lowStockThreshold;
    }

    public void addStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Qty must be > 0");
        stock += qty;
    }

    public void removeStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Qty must be > 0");
        if (qty > stock) throw new IllegalArgumentException("Not enough stock");
        stock -= qty;
    }

    public boolean isLowStock() {
        return stock <= lowStockThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return sku.equals(product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }
}
