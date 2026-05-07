# 🏨 Hotel Microservices

A production-pattern **microservices architecture** for hotel management built with Spring Boot, Spring Cloud, and React. Demonstrates service discovery, API gateway routing, inter-service communication, fault tolerance, and polyglot persistence.

---

## 🏗️ Architecture Overview

```
React Frontend (Port 3000)
        │
        ▼
┌─────────────────────┐
│   API Gateway :8080  │  ← Single entry point for all requests
│  Spring Cloud Gateway│
└─────────────────────┘
        │
        ├──── /users/**   ──────▶  UserService   :8082  (MySQL)
        ├──── /hotels/**  ──────▶  HotelService  :8083  (PostgreSQL)
        └──── /ratings/** ──────▶  RatingService :8084  (MongoDB)

All services register with → Eureka Registry :8761
```

### Inter-Service Communication
When `GET /users/{id}` is called, **UserService** orchestrates:
1. Fetches user from **MySQL**
2. Calls **RatingService** → gets all ratings from **MongoDB**
3. Calls **HotelService** for each rating → gets hotel details from **PostgreSQL**
4. Returns one fully assembled JSON response

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.x |
| Service Registry | Netflix Eureka (Spring Cloud) |
| API Gateway | Spring Cloud Gateway (WebFlux, reactive) |
| Inter-Service Comms | RestTemplate with `@LoadBalanced` |
| Fault Tolerance | Resilience4j — Circuit Breaker + Retry |
| ORM | Spring Data JPA (Hibernate) + Spring Data MongoDB |
| Databases | MySQL · PostgreSQL · MongoDB |
| Config Management | Spring Cloud Config Server |
| Frontend | React 18 (hooks-only, no Redux) |
| Build Tool | Maven |

---

## 📦 Services

### 1. 🔍 Service Registry (Eureka) — `:8761`
- All microservices register here on startup
- Enables service discovery by **name**, not hardcoded IP
- Gateway and services resolve each other via Eureka cache

### 2. 🚪 API Gateway — `:8080`
- Single entry point — the only URL the frontend ever calls
- Path-based routing using Spring Cloud Gateway (WebFlux)
- Load balancing via `lb://SERVICENAME` → Eureka resolution
- Central place for CORS, authentication, and rate limiting

### 3. 👤 User Service — `:8082` · MySQL
- Manages guest profiles
- Aggregates data from RatingService and HotelService at runtime
- `@Transient` ratings field assembled via inter-service HTTP calls
- Protected by `@Retry` — retries up to 3 times with 5s backoff
- Fallback method returns graceful dummy response on failure

### 4. 🏨 Hotel Service — `:8083` · PostgreSQL
- Manages hotel listings — standalone CRUD
- No inter-service dependencies
- `GET /hotels`, `GET /hotels/{id}`, `POST /hotels`

### 5. ⭐ Rating Service — `:8084` · MongoDB
- Stores guest reviews as flexible MongoDB documents
- Filter by userId or hotelId
- `GET /ratings/users/{id}`, `GET /ratings/hotels/{id}`

### 6. ⚙️ Config Server
- Centralised configuration management
- Services pull config on startup instead of managing individual files

---

## 🔄 Request Flow — `GET /users/{id}`

```
Browser → Gateway :8080
       → UserService :8082 → MySQL (fetch user)
       → RatingService :8084 → MongoDB (fetch ratings)
       → HotelService :8083 → PostgreSQL (fetch hotel per rating)
       → Assembled response back to Browser
```

**Response shape:**
```json
{
  "userId": "abc-123",
  "name": "Arjun Sharma",
  "email": "arjun@example.com",
  "ratings": [
    {
      "ratingId": "...",
      "rating": 5,
      "feedback": "Exceptional stay",
      "hotel": {
        "id": "hotel-xyz",
        "name": "The Leela Palace",
        "location": "Bengaluru"
      }
    }
  ]
}
```

---

## 🛡️ Fault Tolerance — Resilience4j

### Retry
```yaml
resilience4j:
  retry:
    instances:
      ratingHotelService:
        max-attempts: 3
        wait-duration: 5000ms
```
Handles transient failures — retries up to 3 times before fallback.

### Circuit Breaker
```yaml
circuitbreaker:
  instances:
    ratingHotelBreaker:
      sliding-window-size: 10
      failure-rate-threshold: 50
      wait-duration-in-open-state: 6s
      permitted-calls-in-half-open-state: 3
```

| State | Behaviour |
|---|---|
| CLOSED | Normal — all calls pass through |
| OPEN | >50% failures — calls return fallback immediately |
| HALF-OPEN | 3 trial calls to test recovery |

---

## 🗄️ Polyglot Persistence

Each service owns its database — no cross-service DB access.

| Service | Database | Reason |
|---|---|---|
| UserService | MySQL | Structured, relational user data |
| HotelService | PostgreSQL | Robust relational with complex query support |
| RatingService | MongoDB | Schema-flexible documents, evolving review structure |

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven
- MySQL, PostgreSQL, MongoDB running locally
- Node.js + npm (for frontend)

### Boot Order (important!)

```bash
# 1. Start Eureka first
cd ServiceRegistry
mvn spring-boot:run

# 2. Start Config Server
cd ConfigServer
mvn spring-boot:run

# 3. Start HotelService
cd HotelService
mvn spring-boot:run

# 4. Start RatingService
cd RatingService
mvn spring-boot:run

# 5. Start UserService
cd UserService
mvn spring-boot:run

# 6. Start API Gateway
cd ApiGateway
mvn spring-boot:run

# 7. Start React Frontend
cd frontend
npm install
npm start
```

### Service URLs

| Service | URL |
|---|---|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| User Service | http://localhost:8082 |
| Hotel Service | http://localhost:8083 |
| Rating Service | http://localhost:8084 |
| React Frontend | http://localhost:3000 |

> All frontend requests go through `http://localhost:8080` — never directly to individual services.

---

## 📡 API Endpoints

### Users
```
GET    /users          → Get all users
GET    /users/{id}     → Get user + ratings + hotels (3-service chain)
POST   /users          → Register new user
```

### Hotels
```
GET    /hotels         → Get all hotels
GET    /hotels/{id}    → Get hotel by ID
POST   /hotels         → Add new hotel
```

### Ratings
```
GET    /ratings              → Get all ratings
GET    /ratings/users/{id}  → Get ratings by user
GET    /ratings/hotels/{id} → Get ratings by hotel
POST   /ratings              → Submit a rating
```

---

## 🔑 Key Design Patterns

| Pattern | Implementation |
|---|---|
| API Gateway | Spring Cloud Gateway routes all traffic |
| Service Discovery | Netflix Eureka — name-based resolution |
| Aggregator | UserService composes response from 3 services |
| Circuit Breaker | Resilience4j prevents cascading failures |
| Database per Service | Each service owns its own database |
| Polyglot Persistence | MySQL + PostgreSQL + MongoDB |
| Fallback | Graceful degradation when services fail |
| Load Balancing | `@LoadBalanced` RestTemplate via Eureka |

---

## 📁 Project Structure

```
Microservices/
├── ApiGateway/          → Spring Cloud Gateway :8080
├── ConfigServer/        → Centralised config management
├── HotelService/        → Hotel CRUD :8083 (PostgreSQL)
├── RatingService/       → Reviews :8084 (MongoDB)
├── ServiceRegistry/     → Eureka Server :8761
└── UserService/         → Guest management :8082 (MySQL)
```

---

## 👨‍💻 Author

**Manish Pradhan**
- GitHub: [@ManishPradhan1](https://github.com/ManishPradhan1)
- Email: manishpradhan37965@gmail.com
