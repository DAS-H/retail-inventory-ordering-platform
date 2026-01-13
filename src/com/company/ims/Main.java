package com.company.ims;

import java.math.BigDecimal;
import java.util.*;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        Store store = new Store();

        while (true) {
            User user = login(store);
            if (user == null) return;

            if (user.getRole() == Role.ADMIN) adminLoop(store, user);
            else userLoop(store, user);
        }
    }

    private static User login(Store store) {
        System.out.println("\n=== Inventory & Order Management ===");
        System.out.println("Login (type 'exit' as username to quit)");

        System.out.print("Username: ");
        String u = in.nextLine().trim();
        if (u.equalsIgnoreCase("exit")) return null;

        System.out.print("Password: ");
        String p = in.nextLine();

        return store.authenticate(u, p).orElseGet(() -> {
            System.out.println("Invalid login.");
            return null;
        });
    }

    private static void adminLoop(Store store, User admin) {
        while (true) {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1) List products");
            System.out.println("2) Add product");
            System.out.println("3) Update product");
            System.out.println("4) Remove product");
            System.out.println("5) Restock product");
            System.out.println("6) Low-stock alerts");
            System.out.println("7) List all orders");
            System.out.println("8) Update order status");
            System.out.println("9) Logout");
            System.out.print("Choose: ");

            String pick = in.nextLine().trim();

            try {
                switch (pick) {
                    case "1" -> listProducts(store);
                    case "2" -> addProduct(store);
                    case "3" -> updateProduct(store);
                    case "4" -> removeProduct(store);
                    case "5" -> restockProduct(store);
                    case "6" -> lowStock(store);
                    case "7" -> listAllOrders(store);
                    case "8" -> updateOrderStatus(store);
                    case "9" -> { return; }
                    default -> System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void userLoop(Store store, User user) {
        while (true) {
            System.out.println("\n=== User Menu (" + user.getUsername() + ") ===");
            System.out.println("1) Browse products");
            System.out.println("2) Place order");
            System.out.println("3) My orders");
            System.out.println("4) View order details");
            System.out.println("5) Cancel my order");
            System.out.println("6) Logout");
            System.out.print("Choose: ");

            String pick = in.nextLine().trim();

            try {
                switch (pick) {
                    case "1" -> listProducts(store);
                    case "2" -> placeOrder(store, user);
                    case "3" -> listMyOrders(store, user);
                    case "4" -> viewOrder(store, user, false);
                    case "5" -> cancelMyOrder(store, user);
                    case "6" -> { return; }
                    default -> System.out.println("Unknown option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void listProducts(Store store) {
        System.out.println("\n--- Product Catalog ---");
        for (Product p : store.listProducts()) {
            System.out.printf("%s | %s | $%s | stock=%d | low<=%d%n",
                    p.getSku(), p.getName(), p.getPrice().toPlainString(), p.getStock(), p.getLowStockThreshold());
        }
    }

    private static void addProduct(Store store) {
        System.out.println("\n--- Add Product ---");
        String sku = ask("SKU");
        String name = ask("Name");
        BigDecimal price = askMoney("Price");
        int stock = askInt("Initial stock", 0, Integer.MAX_VALUE);
        int threshold = askInt("Low stock threshold", 0, Integer.MAX_VALUE);

        store.addProduct(new Product(sku, name, price, stock, threshold));
        System.out.println("Added.");
    }

    private static void updateProduct(Store store) {
        System.out.println("\n--- Update Product ---");
        String sku = ask("SKU to update");
        Product p = store.findProduct(sku).orElseThrow(() -> new IllegalArgumentException("SKU not found"));

        System.out.println("Leave blank to keep current.");
        String name = askOptional("New name (" + p.getName() + ")");
        String priceStr = askOptional("New price (" + p.getPrice().toPlainString() + ")");
        String thresholdStr = askOptional("New low-stock threshold (" + p.getLowStockThreshold() + ")");

        if (!name.isBlank()) p.setName(name);
        if (!priceStr.isBlank()) p.setPrice(new BigDecimal(priceStr));
        if (!thresholdStr.isBlank()) p.setLowStockThreshold(Integer.parseInt(thresholdStr));

        System.out.println("Updated.");
    }

    private static void removeProduct(Store store) {
        System.out.println("\n--- Remove Product ---");
        String sku = ask("SKU to remove");
        store.removeProduct(sku);
        System.out.println("Removed.");
    }

    private static void restockProduct(Store store) {
        System.out.println("\n--- Restock ---");
        String sku = ask("SKU");
        Product p = store.findProduct(sku).orElseThrow(() -> new IllegalArgumentException("SKU not found"));
        int qty = askInt("Qty to add", 1, Integer.MAX_VALUE);
        p.addStock(qty);
        System.out.println("Stock updated. Now: " + p.getStock());
    }

    private static void lowStock(Store store) {
        System.out.println("\n--- Low Stock Alerts ---");
        var list = store.lowStockProducts();
        if (list.isEmpty()) {
            System.out.println("No low-stock items.");
            return;
        }
        for (Product p : list) {
            System.out.printf("ALERT: %s (%s) stock=%d threshold=%d%n",
                    p.getSku(), p.getName(), p.getStock(), p.getLowStockThreshold());
        }
    }

    private static void placeOrder(Store store, User user) {
        System.out.println("\n--- Place Order ---");
        System.out.println("Enter items as: SKU qty. Type 'done' to finish.");

        Map<String, Integer> cart = new LinkedHashMap<>();

        while (true) {
            String line = askOptional("Item");
            if (line.equalsIgnoreCase("done")) break;
            if (line.isBlank()) continue;

            String[] parts = line.trim().split("\\s+");
            if (parts.length != 2) {
                System.out.println("Use: SKU qty");
                continue;
            }

            String sku = parts[0].trim();
            int qty;
            try {
                qty = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                System.out.println("Qty must be a number.");
                continue;
            }

            if (qty <= 0) {
                System.out.println("Qty must be > 0");
                continue;
            }

            cart.merge(sku, qty, Integer::sum);
        }

        if (cart.isEmpty()) {
            System.out.println("No items added.");
            return;
        }

        Order o = store.placeOrder(user.getUsername(), cart);
        System.out.println("Order placed. ID: " + o.getId() + " | Total: $" + o.total().toPlainString());

        var low = store.lowStockProducts();
        if (!low.isEmpty()) {
            System.out.println("\nHeads up: low-stock after this order:");
            for (Product p : low) {
                System.out.printf("- %s (%s) stock=%d threshold=%d%n",
                        p.getSku(), p.getName(), p.getStock(), p.getLowStockThreshold());
            }
        }
    }

    private static void listMyOrders(Store store, User user) {
        System.out.println("\n--- My Orders ---");
        var orders = store.listOrdersForUser(user.getUsername());
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("ID=%d | status=%s | total=$%s%n",
                    o.getId(), o.getStatus(), o.total().toPlainString());
        }
    }

    private static void listAllOrders(Store store) {
        System.out.println("\n--- All Orders ---");
        var orders = store.listAllOrders();
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("ID=%d | user=%s | status=%s | total=$%s%n",
                    o.getId(), o.getCustomerUsername(), o.getStatus(), o.total().toPlainString());
        }
    }

    private static void viewOrder(Store store, User user, boolean adminView) {
        long id = askLong("Order ID");
        Order o = store.findOrder(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!adminView && !o.getCustomerUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("Not your order");
        }

        System.out.println("\n--- Order Details ---");
        System.out.println("ID: " + o.getId());
        System.out.println("User: " + o.getCustomerUsername());
        System.out.println("Status: " + o.getStatus());
        System.out.println("Total: $" + o.total().toPlainString());
        System.out.println("Items:");
        for (OrderItem it : o.getItems()) {
            System.out.printf("  %s | %s | $%s x %d = $%s%n",
                    it.getSku(), it.getName(),
                    it.getUnitPrice().toPlainString(),
                    it.getQuantity(),
                    it.lineTotal().toPlainString());
        }
    }

    private static void cancelMyOrder(Store store, User user) {
        long id = askLong("Order ID to cancel");
        store.cancelOrderAsCustomer(user.getUsername(), id);
        System.out.println("Cancelled (and stock restored).");
    }

    private static void updateOrderStatus(Store store) {
        long id = askLong("Order ID");
        viewOrder(store, new User("admin","x",Role.ADMIN), true);

        System.out.println("\nNext status options depend on current state.");
        System.out.println("1) PAID");
        System.out.println("2) PACKED");
        System.out.println("3) SHIPPED");
        System.out.println("4) DELIVERED");
        System.out.println("5) CANCELLED");
        System.out.print("Choose: ");

        String pick = in.nextLine().trim();
        OrderStatus next;
        switch (pick) {
            case "1" -> next = OrderStatus.PAID;
            case "2" -> next = OrderStatus.PACKED;
            case "3" -> next = OrderStatus.SHIPPED;
            case "4" -> next = OrderStatus.DELIVERED;
            case "5" -> next = OrderStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown status option");
        }

        store.updateOrderStatusAsAdmin(id, next);
        System.out.println("Updated.");
    }

    private static String ask(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine();
            if (s != null && !s.trim().isBlank()) return s.trim();
            System.out.println("Required.");
        }
    }

    private static String askOptional(String label) {
        System.out.print(label + ": ");
        String s = in.nextLine();
        return s == null ? "" : s.trim();
    }

    private static int askInt(String label, int min, int max) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) {
                    System.out.println("Must be between " + min + " and " + max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
            }
        }
    }

    private static long askLong(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                System.out.println("Enter a number.");
            }
        }
    }

    private static BigDecimal askMoney(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                BigDecimal v = new BigDecimal(s);
                if (v.signum() < 0) {
                    System.out.println("Must be >= 0");
                    continue;
                }
                return v;
            } catch (Exception e) {
                System.out.println("Enter a valid decimal (example: 12.99)");
            }
        }
    }
}
