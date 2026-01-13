Inventory & Order Management System

A Java-based console application that simulates real-world inventory and order workflows commonly used in retail and e-commerce systems. This project focuses on business logic, role-based access control, and order lifecycle management, making it suitable for backend and entry-level software engineering interviews.

Overview
The system models how companies manage products, stock levels, and customer orders while enforcing clear responsibilities between admins and users. Core rules such as stock validation, order status transitions, and cancellation limits are handled programmatically to prevent invalid operations.

Features
Product catalog with SKU-based identification
Real-time stock tracking, restocking, and low-stock alerts
Order placement with stock availability validation
Automatic stock deduction on order creation
Safe order cancellation with stock rollback
Strict order lifecycle: PLACED → PAID → PACKED → SHIPPED → DELIVERED
Role-based access control
Admin capabilities: manage inventory, view all orders, update order statuses, monitor low-stock alerts
User capabilities: browse products, place orders, view personal orders, cancel eligible orders

Tech Stack
Language: Java
IDE: IntelliJ IDEA
Programming style: Object-Oriented Programming
Data storage: In-memory (designed to highlight business logic rather than persistence)

Project Structure
src/com/company/ims
Main.java
Store.java
Product.java
Order.java
OrderItem.java
OrderStatus.java
User.java
Role.java

How to Run
Open the project in IntelliJ IDEA
Ensure the src directory is marked as Sources Root
Run Main.java
Interact with the application through the console menu

Default Credentials
Admin
Username: admin
Password: admin123

User
Username: Omkar
Password: user123

Repository Note
This project is hosted on my college GitHub account. My primary GitHub account was temporarily banned due to an account duplication issue, so this repository is used to ensure continued access and visibility of the project.

Future Improvements
Persistent storage using files or a database
REST API version using Spring Boot
Unit testing with JUnit
Improved authentication and user registration
