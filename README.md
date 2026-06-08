# 🐄 Cattle Feed Demand Management System
### Developed during internship at Amul — Kaira District Co-operative Milk Producers' Union Ltd. (GCMMF), Anand

A production-grade **Spring Boot** backend system that digitizes and automates cattle feed distribution, allocation, and tracking across Amul's dairy cooperative network. The system serves three role-based user types — **Admin, Secretary, and Farmer** — with full security, biometric verification, and real-time inventory management.

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 3.x (Java 17) |
| Security | Spring Security · JWT · BCrypt · AES-256 |
| ORM & Database | Spring Data JPA · MySQL |
| Config Encryption | Jasypt |
| File Handling | Multipart File Upload |
| Build Tool | Maven |

---

## ✨ Key Features

### 🔐 Security & Authentication
- **Stateless JWT Authentication** — custom `JwtAuthenticationFilter` intercepts every request, extracts and validates JWT, and injects user context (Admin / Secretary / Farmer)
- **Role-Based Access Control (RBAC)** — `@PreAuthorize` guards on all endpoints; roles strictly enforced at method level via `@EnableMethodSecurity`
- **BCrypt Password Hashing** — all credentials hashed before storage
- **AES-256 Aadhaar Encryption** — sensitive Aadhaar numbers encrypted at rest using `AesEncryptionUtil`
- **Jasypt Config Encryption** — database passwords and API secrets encrypted inside `application.properties`
- **Brute Force Protection** — `LoginAttemptService` locks accounts after consecutive failed attempts (15-minute cooldown window)

### 🤳 Biometric & eKYC Verification
- **Face Identification** — verifies farmer identity using 128-dimensional face embeddings; similarity calculated via **Euclidean Distance** algorithm (threshold `< 0.41`) to prevent proxy feed distribution
- **Aadhaar eKYC Simulation** — OCR-based Aadhaar number extraction and OTP email verification flow using concurrent in-memory cache

### 📦 Inventory & Feed Management
- **Cycle-based Distribution** — feed distributed in 3 cycles per month (1–10, 11–20, 21–31)
- **District-level Stock Control** — stock tracked per zone/district per feed type; low-stock alerts when quantity falls below safety threshold
- **Atomic Transactions** — `@Transactional` ensures stock deduction, wallet debit, transaction log, and status update all succeed or all rollback together

### 👨‍🌾 Farmer & Wallet System
- Farmer registration with livestock details (cows, buffaloes, goats, camels), photo, signature, and biometric enrollment
- Digital wallet linked to each farmer; automatically debited on feed request approval
- Full transaction audit trail with cycle tracking

---

## 🗂️ Database Schema (14 Entities)

| Table | Description |
|---|---|
| `admin` | System administrators |
| `farmers` | Dairy farmers with wallet, livestock counts, AES-encrypted Aadhaar |
| `secretaries` | Village-level cooperative society representatives |
| `authentication_logs` | Full audit log of LOGIN / LOGOUT / FAILED_LOGIN events with IP address |
| `biometric_data` | 128-float face embeddings stored as JSON/Base64 per user |
| `cattle_feed` | Feed types (name, type, price per bag, weight, stock capacity) |
| `stock` | District-level stock quantities per feed type |
| `stock_movement` | IN/OUT inventory movement logs with cycle and emergency flag |
| `feed_requests` | Feed request lifecycle (PENDING → APPROVED / REJECTED) |
| `transaction` | Wallet deduction records per farmer per cycle |
| `notification` | In-app alerts for request status updates |
| `zones` | Geographic zone definitions |
| `talukas` | Taluka-level geography, linked to zones |
| `villages` | Village-level geography, linked to talukas |

---

## 🌐 REST API Overview

### Authentication — `/api/auth`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Admin/Secretary login — returns JWT |
| POST | `/api/auth/farmer/login` | Farmer portal login |
| POST | `/api/auth/reset-password` | Password reset via DOB + Aadhaar match |

### Biometric — `/api/biometric`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/biometric/face/verify` | Verify face embedding against registered data |

### eKYC — `/api/ekyc`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ekyc/aadhaar/ocr` | Simulate Aadhaar OCR extraction |
| POST | `/api/ekyc/email/send-otp` | Send OTP to registered email |
| POST | `/api/ekyc/email/verify-otp` | Verify OTP |

### Secretary — `/api/secretary`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/secretary/farmer/register` | Full farmer registration with photo, signature, biometrics |
| PUT | `/api/secretary/farmer/update/{id}` | Update farmer details |
| GET | `/api/secretary/feed-request/pending` | Get pending requests for secretary's society |
| POST | `/api/secretary/feed-request/approve/{id}` | Approve request — deducts stock, debits wallet, logs transaction |
| POST | `/api/secretary/feed-request/reject/{id}` | Reject request with notification |

### Admin — `/api/admin`
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/admin/stock/add` | Restock feed for a zone/district |
| GET | `/api/admin/stock/low-alerts` | Items below safety threshold |
| GET | `/api/admin/dashboard` | Global stats — total stock, farmers, pending requests |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/HoneyVanjani/cattle-feed-demand-management.git
cd cattle-feed-demand-management

# 2. Configure your database
# Edit src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/cattle_feed_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# 3. Build and run
mvn clean install
mvn spring-boot:run
```

> ⚠️ **Note:** Sensitive config values (DB passwords, JWT secret, Jasypt key) are not included in this repository for security reasons.

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/amul/cattlefeed/
│   │   ├── controller/       # REST Controllers
│   │   ├── entity/           # 14 JPA Entities
│   │   ├── repository/       # Spring Data JPA Repositories
│   │   ├── service/          # Business Logic
│   │   ├── security/         # JWT Filter, Security Config, RBAC
│   │   └── util/             # AES Encryption, helpers
│   └── resources/
│       └── application.properties
```

---

## 🔒 Security Notes

- All Aadhaar numbers are **AES-256 encrypted** before database storage
- Passwords use **BCrypt** hashing — never stored in plain text
- JWT tokens are **stateless** — no server-side session storage
- Failed login attempts are tracked and accounts are **auto-locked** after threshold
- This repository does **not** contain any real farmer data or credentials

---

## 👥 Project

| Role | Contributor |
|---|---|
| **Backend** | [Honey Vanjani](https://github.com/HoneyVanjani/cattle-feed-demand-management) |
| **Frontend** | [Milan Parmar](https://github.com/Milan1808/cattle-feed-demand-management-system) |

---

