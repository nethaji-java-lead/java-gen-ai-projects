# Java Microservices Project

A hands-on **Microservices Architecture** project built with **Java, Spring Boot, Apache Kafka, and H2 Database**.

The project demonstrates how multiple independent microservices communicate through REST APIs and event-driven messaging using Kafka, while maintaining their own data storage.

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │      Client         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Order Service    │
                         │   Spring Boot       │
                         └───────┬─────┬───────┘
                                 │     │
                    REST / Event │     │ Event
                                 │     │
                                 ▼     ▼
                         ┌───────────┐ ┌──────────────┐
                         │  Kafka    │ │ H2 Database  │
                         └─────┬─────┘ └──────────────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
           ┌─────────────────┐   ┌─────────────────┐
           │ Payment Service │   │ Inventory       │
           │ Spring Boot     │   │ Service         │
           └────────┬────────┘   └────────┬────────┘
                    │                     │
                    ▼                     ▼
             ┌────────────┐        ┌────────────┐
             │ H2 Database│        │ H2 Database│
             └────────────┘        └────────────┘
```

## 🚀 Technologies

* **Java**
* **Spring Boot**
* **Spring Web / REST APIs**
* **Spring Data JPA**
* **Apache Kafka**
* **H2 Database**
* **Maven**
* **Lombok**
* **Docker** *(if applicable)*

## 📦 Microservices

### 1. Order Service

Responsible for creating and managing customer orders.

Responsibilities:

* Create orders
* Retrieve orders
* Persist order information
* Publish order events to Kafka
* Consume relevant events from other services

### 2. Payment Service

Responsible for processing payments associated with orders.

Responsibilities:

* Process payments
* Maintain payment status
* Persist payment information
* Consume order/payment events
* Publish payment events

### 3. Inventory Service

Responsible for managing product inventory.

Responsibilities:

* Check product availability
* Reserve inventory
* Update stock quantities
* Consume order events
* Publish inventory events

## 🔄 Event-Driven Communication

The services use **Apache Kafka** for asynchronous communication.

Example flow:

```text
Order Created
     │
     ▼
Order Service
     │
     │ OrderCreatedEvent
     ▼
   Kafka
     │
     ├───────────────┐
     ▼               ▼
Payment Service   Inventory Service
     │               │
     ▼               ▼
Payment Event    Inventory Event
     │               │
     └───────┬───────┘
             ▼
           Kafka
```

This approach helps reduce direct coupling between microservices and allows services to process events independently.

## 🗄️ Database

Each microservice uses its own **H2 database**.

This follows the **Database-per-Service** pattern commonly used in microservice architectures.

```text
Order Service      → Order H2 Database
Payment Service    → Payment H2 Database
Inventory Service  → Inventory H2 Database
```

Services do not directly access another service's database.

## 📁 Project Structure

```text
java-microservices/
│
├── order-service/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── payment-service/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── inventory-service/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
└── README.md
```

## ⚙️ Prerequisites

Make sure the following are installed:

* Java 17+
* Maven 3.8+
* Apache Kafka
* Git

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

## ▶️ Running the Project

### 1. Start Kafka

Start your Kafka broker using your preferred Kafka setup.

For a local environment, Kafka should be available at:

```text
localhost:9092
```

### 2. Start the Services

Start each Spring Boot application independently.

```bash
cd order-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

```bash
cd inventory-service
mvn spring-boot:run
```

## 🔌 Example API

Create an order:

```http
POST /api/orders
Content-Type: application/json
```

Example request:

```json
{
  "productId": "P1001",
  "quantity": 2,
  "customerId": "C1001"
}
```

The Order Service persists the order and publishes an `OrderCreatedEvent` to Kafka.

## 📨 Example Kafka Event

```json
{
  "eventId": "e12345",
  "eventType": "ORDER_CREATED",
  "orderId": "O10001",
  "customerId": "C1001",
  "productId": "P1001",
  "quantity": 2
}
```

## 🎯 Key Microservices Concepts Demonstrated

This project is intended to demonstrate practical microservices concepts including:

* Microservices architecture
* REST API communication
* Event-driven architecture
* Apache Kafka
* Kafka producers and consumers
* Kafka topics and partitions
* Consumer groups
* Event-based communication
* Database-per-Service
* Spring Data JPA
* Transaction management
* Idempotent event processing
* Exception handling
* Service independence
* Asynchronous processing

## 🔮 Future Enhancements

Planned improvements may include:

* [ ] API Gateway
* [ ] Service Discovery
* [ ] Centralized Configuration
* [ ] Docker Compose
* [ ] PostgreSQL
* [ ] Redis caching
* [ ] Kafka retry mechanism
* [ ] Dead Letter Queue (DLQ)
* [ ] Distributed tracing
* [ ] Prometheus & Grafana
* [ ] Kubernetes deployment
* [ ] CI/CD pipeline
* [ ] Integration tests with Testcontainers

## 📚 Purpose

This repository is created as a practical reference for building **production-style Java Spring Boot microservices** using synchronous REST communication and asynchronous Kafka-based event processing.

The goal is to demonstrate how independently deployable services can communicate reliably while maintaining clear ownership of their data and business responsibilities.

---

### 👨‍💻 Author

**Nethaji Java Lead**

Java | Spring Boot | Microservices | Kafka | AWS | Kubernetes
