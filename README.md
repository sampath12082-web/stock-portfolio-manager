# Stock Portfolio Manager

A full-stack stock portfolio management application built with **Java 21**, **Spring Boot**, **PostgreSQL**, **Flyway**, and **React** (planned) to help investors track holdings, manage transactions, monitor portfolio performance, and integrate real-time market data.

---

## Features

### Implemented

#### Stock Management

* Create Stock
* Get Stock by Symbol
* List All Stocks

#### Holding Management

* Create Holding
* Associate Holdings with Stocks

#### Backend Infrastructure

* Spring Boot 3
* Spring Data JPA
* PostgreSQL
* Flyway Database Migrations
* RESTful APIs
* Layered Architecture (Controller → Service → Repository)

---

## Planned Features

### Portfolio Management

* Portfolio Summary
* Profit & Loss Calculation
* Portfolio Valuation
* Asset Allocation Dashboard

### Market Data Integration

* Groww API Integration
* Stock Search
* Real-Time Price Updates
* Historical Price Data

### Transactions

* Buy Transactions
* Sell Transactions
* Transaction History
* Average Cost Calculation

### Frontend

* React + Vite
* Material UI
* Portfolio Dashboard
* Interactive Charts
* Responsive Design

### Security

* JWT Authentication
* User Management
* Role-Based Access Control

---

## Technology Stack

### Backend

| Technology      | Version |
| --------------- | ------- |
| Java            | 21      |
| Spring Boot     | 3.x     |
| Spring Data JPA | Latest  |
| PostgreSQL      | 18      |
| Flyway          | Latest  |
| Maven           | Latest  |

### Frontend (Planned)

| Technology  | Version |
| ----------- | ------- |
| React       | Latest  |
| Vite        | Latest  |
| Material UI | Latest  |
| React Query | Latest  |
| Axios       | Latest  |

---

## Project Structure

```text
src/main/java/com/stocks/myportfolio

├── controller
├── service
│   └── impl
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── common
│   └── enums
├── config
├── exception
└── client
```

---

## Database Schema

### Stock

```text
Stock
├── id
├── symbol
├── company_name
├── exchange
├── sector
└── industry
```

### Holding

```text
Holding
├── id
├── stock_id
├── quantity
├── average_buy_price
└── invested_amount
```

### Transaction History (Planned)

```text
TransactionHistory
├── id
├── stock_id
├── transaction_type
├── quantity
├── price
└── transaction_date
```

---

## Running Locally

### Prerequisites

* Java 21
* PostgreSQL
* Maven

### Clone Repository

```bash
git clone https://github.com/<your-username>/stock-portfolio-manager.git
cd stock-portfolio-manager
```

### Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE myportfolio;
```

Configure datasource:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/myportfolio
    username: your_username
    password: your_password
```

### Start Application

```bash
./mvnw spring-boot:run
```

Application will start on:

```text
http://localhost:8081
```

---

## API Endpoints

### Stocks

#### Create Stock

```http
POST /api/stocks
```

Request:

```json
{
  "symbol": "RELIANCE",
  "companyName": "Reliance Industries Ltd",
  "exchange": "NSE",
  "sector": "Energy",
  "industry": "Oil & Gas"
}
```

#### Get Stock

```http
GET /api/stocks/{symbol}
```

Example:

```http
GET /api/stocks/RELIANCE
```

#### Get All Stocks

```http
GET /api/stocks
```

---

### Holdings

#### Create Holding

```http
POST /api/holdings
```

Request:

```json
{
  "symbol": "RELIANCE",
  "quantity": 10,
  "averageBuyPrice": 1450.50
}
```

---

## Development Roadmap

### Version 0.1.0

* [x] Spring Boot Setup
* [x] PostgreSQL Integration
* [x] Flyway Migrations
* [x] Stock APIs
* [x] Holding APIs

### Version 0.2.0

* [ ] Portfolio Summary API
* [ ] Global Exception Handling
* [ ] Swagger/OpenAPI Documentation
* [ ] Validation Enhancements

### Version 0.3.0

* [ ] Groww API Integration
* [ ] Stock Search API
* [ ] Live Market Data

### Version 0.4.0

* [ ] React Dashboard
* [ ] Portfolio Analytics
* [ ] Interactive Charts

### Version 1.0.0

* [ ] Authentication & Authorization
* [ ] Watchlists
* [ ] Notifications
* [ ] Production Deployment

---

## Architecture

```text
Client
   ↓
Controller
   ↓
Service
   ↓
Repository
   ↓
PostgreSQL
```

External Integrations:

```text
Groww API
   ↓
GrowwClient
   ↓
Service Layer
```

---

## License

This project is licensed under the MIT License.

---

## Author

Built as a learning and portfolio project to demonstrate modern full-stack application development using Spring Boot, PostgreSQL, React, and financial market integrations.
