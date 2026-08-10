# FoodExpress - Full-Stack Food Delivery Web Application

FoodExpress is a production-quality food delivery web platform inspired by Zomato and Swiggy, built with **Spring Boot 3**, **Thymeleaf**, **Spring Data JPA**, **Spring Security**, **Bootstrap 5**, **MySQL**, and **H2 Database**.

---

## 🌟 Features Overview

### 🍔 Customer Features
- **Modern Responsive UI**: Clean, warm food theme with micro-animations and smooth card layouts.
- **Restaurant Discovery**: Search restaurants by name, cuisine, rating, or delivery time.
- **Filtered Search**: Filter restaurants by cuisine (Pizza, Biryani, Burgers, Chinese, South Indian, Desserts, etc.), minimum rating, and sorting.
- **Categorized Menu View**: View food items grouped by Starters, Main Course, Biryani, Pizzas, Desserts, and Beverages with Vegetarian indicators.
- **Server-Validated Cart**: Add items, update quantities, remove items, and enforce single-restaurant order constraints.
- **Coupons & Promo Codes**: Apply discount coupons (`WELCOME50`, `FOOD100`, `SUPER20`) during checkout with live discount calculation.
- **Delivery Address Management**: Save multiple delivery addresses with default selection.
- **Order Placement & Status Timeline**: Place Cash on Delivery (COD) orders with a visual order tracker (`PLACED` -> `CONFIRMED` -> `PREPARING` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).
- **Order History & Cancellation**: View past orders and cancel orders when eligible.

### 🛡️ Admin Dashboard Features
- **Executive Summary Dashboard**: Overview metrics for total revenue, today's revenue, registered users, total orders, active restaurants, and pending/delivered order counters.
- **Restaurant Management**: Create, update, toggle active status, and delete restaurants.
- **Food Catalog Management**: Add/edit dishes, update prices, toggle availability, and manage food categories.
- **Order Timeline Manager**: Filter orders by status and update order status timelines in real-time.
- **User Directory**: View registered customer accounts and role assignments.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.5, Spring Web, Spring Security, Spring Data JPA, Lombok, Jakarta Validation.
- **Frontend**: Thymeleaf, Bootstrap 5, Bootstrap Icons, Vanilla JavaScript (ES6+), Custom CSS3.
- **Database**: H2 (In-Memory for zero-configuration out-of-the-box local testing) + MySQL support.

---

## 🔐 Sample Login Credentials

On first launch, initial seed data is automatically populated with sample restaurants, food dishes, coupons, and the following demo user accounts:

| User Type | Email | Password | Role |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@foodexpress.com` | `user123` | `ROLE_CUSTOMER` |
| **Admin** | `admin@foodexpress.com` | `admin123` | `ROLE_ADMIN` |

---

## 🚀 Quick Start & How to Run

### Prerequisites
1. **Java Development Kit (JDK 21)** installed and set in `JAVA_HOME`.

### Running Locally (Zero Configuration)
The application comes pre-configured with an **H2 In-Memory Database**, so no database setup is required to run and test immediately!

1. Open a terminal in the project root directory.
2. Build and run using the Maven Wrapper:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

3. Open your browser and navigate to:
   - **Main Website**: `http://localhost:8080`
   - **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:foodexpressdb`, Username: `sa`, Password: leave blank)

---

## 🗄️ MySQL Database Setup (Optional for Production)

To connect FoodExpress to a local MySQL instance:

1. Create a database named `foodexpress`:
   ```sql
   CREATE DATABASE foodexpress;
   ```
2. Open `src/main/resources/application.properties`.
3. Uncomment the MySQL section and set your MySQL username and password:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   spring.jpa.hibernate.ddl-auto=update
   ```
4. Re-run the application:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```

---

## 📂 Key Architecture & Package Structure

```
src/main/java/com/example/food/delivery/
├── controller/         # Spring MVC Web Controllers (Home, Auth, Restaurant, Cart, Checkout, Order, Admin)
├── dto/                # Data Transfer Objects & Validation Beans
├── entity/             # JPA Database Entities (User, Restaurant, FoodItem, Order, Cart, Coupon, Address)
├── repository/         # Spring Data JPA Interfaces
├── security/           # Spring Security Config & CustomUserDetailsService
└── service/            # Business Logic & Seed Data Initializer
```

---

## 💳 Future Payment Integration Roadmap

The payment architecture uses abstract `PaymentMethod` and `PaymentStatus` enums (`CASH_ON_DELIVERY`, `ONLINE`, `PENDING`, `PAID`). It is designed to easily integrate payment gateways like **Razorpay** or **Stripe** via Spring service interface implementation without altering database models or UI flow.
