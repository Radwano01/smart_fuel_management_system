# Smart Fuel Management System - API Documentation

**Version:** 1.0.0  
**Last Updated:** May 1, 2026  
**Base URL:** `http://localhost:8080` (or configured server)

---

## Overview

This document provides comprehensive API documentation for the Smart Fuel Management System. The system is built on a microservices architecture with the following user-facing services:

- **Auth Service** - User authentication and token validation
- **User Service** - User profile management
- **Wallet Service** - Digital wallet and balance management
- **Vehicle Service** - User vehicle management
- **Station Service** - Fuel station information
- **Transaction Service** - Transaction history and management
- **Payment Method Service** - Payment card management
- **Payment Preference Service** - User payment method preferences
- **Password Reset Token Service** - Password reset functionality

> **Note:** Admin APIs (routes under `/api/v1/admin/*`) and internal service-to-service APIs (routes under `/internal/*`) are excluded from this documentation. The Payment Service operates as an internal service only for transaction processing.

**Internal-Only Services (Service-to-Service Communication):**
- **Payment Service** - Handles payment processing and eligibility checks (called by Fuel Session Service)

---

## Table of Contents

1. [Authentication](#authentication)
2. [Auth Service APIs](#auth-service-apis)
3. [User Service APIs](#user-service-apis)
4. [Wallet Service APIs](#wallet-service-apis)
5. [Vehicle Service APIs](#vehicle-service-apis)
6. [Station Service APIs](#station-service-apis)
7. [Transaction Service APIs](#transaction-service-apis)
8. [Payment Method Service APIs](#payment-method-service-apis)
9. [Payment Preference Service APIs](#payment-preference-service-apis)
10. [Password Reset Token Service APIs](#password-reset-token-service-apis)
11. [Internal Services](#internal-services)
12. [Status Codes & Error Handling](#status-codes--error-handling)
13. [Common Data Types](#common-data-types)

---

## Authentication

### JWT Token Usage

Most endpoints require JWT authentication via the `Authorization` header:

```
Authorization: Bearer <your_jwt_token>
```

The token is obtained by:
1. Registering via `/api/v1/auth/register`
2. Logging in via `/api/v1/auth/login`

Public endpoints that do NOT require authentication:
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `POST /api/v1/users/password-reset/request`
- `GET /api/v1/stations` (list all stations)
- `GET /api/v1/stations/{id}` (get station by ID)
- `GET /api/v1/stations/city/{city}` (get stations by city)
- `GET /api/v1/stations/{id}/price` (get fuel price)

---

## Auth Service APIs

**Base URL:** `/api/v1/auth`

### 1. Register

Creates a new user with authentication credentials.

**Endpoint:** `POST /api/v1/auth/register`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "password": "SecurePassword123!",
  "role": "USER"
}
```

**Response:** `201 Created`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid input
- `409 Conflict` - User already exists

---

### 2. Login

Authenticates user and returns JWT tokens.

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `404 Not Found` - User not found

---

### 3. Validate Token

Validates whether a JWT token is valid and not expired.

**Endpoint:** `GET /api/v1/auth/validate`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | string | Yes | JWT token to validate |

**Response:** `200 OK`
```json
true
```

**Error Responses:**
- `400 Bad Request` - Token missing
- `401 Unauthorized` - Token invalid or expired

---

### 4. Get Current User ID

Extracts the user ID from the JWT token in the Authorization header.

**Endpoint:** `GET /api/v1/auth/me`

**Request Headers:**
| Header | Required | Description |
|--------|----------|-------------|
| Authorization | Yes | Bearer token |

**Response:** `200 OK`
```json
"550e8400-e29b-41d4-a716-446655440000"
```

**Error Responses:**
- `401 Unauthorized` - Invalid or missing token

---

## User Service APIs

**Base URL:** `/api/v1/users`

### 1. Register User

Creates a new user account.

**Endpoint:** `POST /api/v1/users/register`

**Request Body:**
```json
{
  "fullName": "John Doe",
  "phoneNumber": "+1-555-0123",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `201 Created`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid input
- `409 Conflict` - Email or phone number already exists

---

### 2. Login User

Authenticates a user and returns JWT tokens.

**Endpoint:** `POST /api/v1/users/login`

**Request Body:**
```json
{
  "phoneNumber": "+1-555-0123",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `401 Unauthorized` - Invalid credentials
- `404 Not Found` - User not found

---

### 3. Get User Profile

Retrieves the authenticated user's profile information including wallet balance.

**Endpoint:** `GET /api/v1/users/{userId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "John Doe",
  "phoneNumber": "+1-555-0123",
  "wallet": {
    "balance": 5000.00,
    "currency": "USD",
    "statusType": "ACTIVE"
  }
}
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - User not found

---

### 4. Update User Profile

Updates user's personal information.

**Endpoint:** `PUT /api/v1/users/{userId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Request Body:** (all fields optional)
```json
{
  "fullName": "John Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "+1-555-0456"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid token
- `409 Conflict` - Email or phone number already in use
- `404 Not Found` - User not found

---

### 5. Update Password

Changes the user's password.

**Endpoint:** `PUT /api/v1/users/{userId}/password`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Request Body:**
```json
{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword456!"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` - Old password incorrect or new password invalid
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - User not found

---

### 6. Request Password Reset

Initiates password reset process by sending reset token to user's email.

**Endpoint:** `POST /api/v1/users/password-reset/request`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| email | string | Yes | User's email address |

**Response:** `200 OK`
```
No body
```

**Error Responses:**
- `404 Not Found` - Email not found in system

---

### 7. Reset Password

Resets user's password using a valid reset token.

**Endpoint:** `POST /api/v1/users/{userId}/password-reset`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | string | Yes | Password reset token (from email) |
| newPassword | string | Yes | New password |

**Response:** `200 OK`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid token or password
- `404 Not Found` - User not found
- `401 Unauthorized` - Token expired or invalid

---

## Wallet Service APIs

**Base URL:** `/api/v1/wallet`

### 1. Create Wallet

Creates a new wallet for the authenticated user (typically called automatically during user registration).

**Endpoint:** `POST /api/v1/wallet`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `201 Created`
```
No body
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `409 Conflict` - Wallet already exists for user

---

### 2. Get Wallet Details

Retrieves the authenticated user's wallet information.

**Endpoint:** `GET /api/v1/wallet`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
{
  "balance": 5000.00,
  "currency": "USD",
  "statusType": "ACTIVE"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Wallet not found

---

### 3. Update Wallet Balance

Updates the authenticated user's wallet balance (top-up, withdraw, refund).

**Endpoint:** `POST /api/v1/wallet/update`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Request Body:**
```json
{
  "amount": 100.00,
  "type": "TOP_UP"
}
```

**TransactionType enum values:**
- `TOP_UP` - Add funds to wallet
- `WITHDRAW` - Withdraw funds from wallet
- `REFUND` - Refund transaction
- `BONUS` - Bonus credit

**Response:** `200 OK`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid amount or type
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Wallet not found
- `409 Conflict` - Insufficient balance (for withdrawals)

---

### 4. Deactivate Wallet

Deactivates the authenticated user's wallet.

**Endpoint:** `PATCH /api/v1/wallet/deactivate`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Wallet not found

---

### 5. Apply Bonus

Applies a promotional bonus to a user's wallet.

**Endpoint:** `POST /api/v1/wallet/bonus`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 50.00
}
```

**Response:** `200 OK`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid amount
- `404 Not Found` - User or wallet not found

---

## Vehicle Service APIs

**Base URL:** `/api/v1/vehicles`

### 1. Create Vehicle

Adds a new vehicle to the authenticated user's account.

**Endpoint:** `POST /api/v1/vehicles`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Request Body:**
```json
{
  "plateNumber": "ABC-1234",
  "brand": "Toyota",
  "model": "Camry",
  "fuelType": "PETROL"
}
```

**FuelType enum values:**
- `PETROL`
- `DIESEL`
- `PREMIUM`

**Response:** `201 Created`
```json
"550e8400-e29b-41d4-a716-446655440000"
```

**Error Responses:**
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid token
- `409 Conflict` - Plate number already registered

---

### 2. List Vehicles

Retrieves all vehicles for the authenticated user.

**Endpoint:** `GET /api/v1/vehicles`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "brand": "Toyota",
    "model": "Camry",
    "status": "ACTIVE"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "brand": "Honda",
    "model": "Civic",
    "status": "INACTIVE"
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token

---

### 3. Get Vehicle Details

Retrieves details of a specific vehicle.

**Endpoint:** `GET /api/v1/vehicles/{vehicleId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "plateNumber": "ABC-1234",
  "brand": "Toyota",
  "model": "Camry",
  "fuelType": "PETROL",
  "status": "ACTIVE",
  "createdAt": "2025-05-01T10:30:00Z",
  "updatedAt": "2025-05-01T10:30:00Z"
}
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Vehicle not found or not owned by user

---

### 4. Update Vehicle

Updates a vehicle's information.

**Endpoint:** `PATCH /api/v1/vehicles/{vehicleId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Request Body:** (all fields optional)
```json
{
  "brand": "Toyota",
  "model": "Camry 2024",
  "fuelType": "PETROL"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Vehicle not found or not owned by user

---

### 5. Deactivate Vehicle

Deactivates a vehicle (marks it as no longer in use).

**Endpoint:** `PATCH /api/v1/vehicles/{vehicleId}/deactivate`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Vehicle not found or not owned by user

---

## Station Service APIs

**Base URL:** `/api/v1/stations`

### 1. List All Stations

Retrieves all available fuel stations.

**Endpoint:** `GET /api/v1/stations`

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Shell Station Downtown",
    "city": "New York",
    "address": "123 Main Street",
    "active": true,
    "pumpCount": 8,
    "availableFuelTypes": ["PETROL", "DIESEL", "PREMIUM"],
    "dieselPrice": 3.25,
    "regularPrice": 3.15,
    "premiumPrice": 3.35
  }
]
```

**Error Responses:**
- `500 Internal Server Error` - Server error

---

### 2. Get Station by ID

Retrieves details of a specific fuel station.

**Endpoint:** `GET /api/v1/stations/{id}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Station's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Shell Station Downtown",
  "city": "New York",
  "address": "123 Main Street",
  "active": true,
  "pumpCount": 8,
  "availableFuelTypes": ["PETROL", "DIESEL", "PREMIUM"],
  "dieselPrice": 3.25,
  "regularPrice": 3.15,
  "premiumPrice": 3.35
}
```

**Error Responses:**
- `404 Not Found` - Station not found

---

### 3. Get Stations by City

Retrieves all stations in a specific city.

**Endpoint:** `GET /api/v1/stations/city/{city}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| city | string | Yes | City name |

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Shell Station Downtown",
    "city": "New York",
    "address": "123 Main Street",
    "active": true,
    "pumpCount": 8,
    "availableFuelTypes": ["PETROL", "DIESEL", "PREMIUM"],
    "dieselPrice": 3.25,
    "regularPrice": 3.15,
    "premiumPrice": 3.35
  }
]
```

**Error Responses:**
- `404 Not Found` - No stations found in city

---

### 4. Get Fuel Price

Retrieves the current price for a specific fuel type at a station.

**Endpoint:** `GET /api/v1/stations/{id}/price`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | Station's unique identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| type | FuelType | Yes | Fuel type (PETROL, DIESEL, PREMIUM) |

**Response:** `200 OK`
```json
3.25
```

**Error Responses:**
- `404 Not Found` - Station not found or fuel type not available

---

## Transaction Service APIs

**Base URL:** `/api/v1/transactions`

### 1. Create Transaction

Creates a new transaction record.

**Endpoint:** `POST /api/v1/transactions`

**Request Body:**
```json
{
  "stationId": "550e8400-e29b-41d4-a716-446655440000",
  "vehicleId": "660e8400-e29b-41d4-a716-446655440001",
  "plateNumber": "ABC-1234"
}
```

**Response:** `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "amount": 45.50,
  "type": "FUEL_PURCHASE",
  "status": "PENDING"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid input
- `404 Not Found` - Station or vehicle not found

---

### 2. Update Transaction Status

Updates the status of a transaction.

**Endpoint:** `PATCH /api/v1/transactions/{transactionId}/status`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionId | UUID | Yes | Transaction's unique identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | TransactionStatusType | Yes | New status (SUCCESS, PENDING, FAILED) |

**Response:** `200 OK`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid status
- `404 Not Found` - Transaction not found

---

### 3. Get User Transactions

Retrieves all transactions for the authenticated user.

**Endpoint:** `GET /api/v1/transactions`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "amount": 45.50,
    "type": "FUEL_PURCHASE",
    "status": "SUCCESS"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "amount": 52.75,
    "type": "FUEL_PURCHASE",
    "status": "PENDING"
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token

---

### 4. Get Transaction Details

Retrieves details of a specific transaction.

**Endpoint:** `GET /api/v1/transactions/{transactionId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionId | UUID | Yes | Transaction's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "amount": 45.50,
  "type": "FUEL_PURCHASE",
  "status": "SUCCESS"
}
```

**Error Responses:**
- `404 Not Found` - Transaction not found

---

## Payment Method Service APIs

**Base URL:** `/api/v1/payment-methods`

### 1. Add Payment Card

Adds a new payment card to the authenticated user's account.

**Endpoint:** `POST /api/v1/payment-methods`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Request Body:**
```json
{
  "cardHolderName": "John Doe",
  "cardNumber": "4532-1111-2222-3333",
  "expiryDate": "12/25"
}
```

**Response:** `201 Created`
```
No body
```

**Error Responses:**
- `400 Bad Request` - Invalid card details
- `401 Unauthorized` - Missing or invalid token
- `409 Conflict` - Card already exists

---

### 2. List Payment Methods

Retrieves all payment cards for the authenticated user.

**Endpoint:** `GET /api/v1/payment-methods`

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "paymentMethodType": "CREDIT_CARD",
    "cardHolderName": "John Doe",
    "maskedNumber": "****-****-****-3333",
    "isDefault": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "paymentMethodType": "DEBIT_CARD",
    "cardHolderName": "John Doe",
    "maskedNumber": "****-****-****-4444",
    "isDefault": false
  }
]
```

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token

---

### 3. Delete Payment Method

Removes a payment card from the authenticated user's account.

**Endpoint:** `DELETE /api/v1/payment-methods/{methodId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodId | UUID | Yes | Payment method's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Payment method not found

---

### 4. Set Default Payment Method

Sets a payment method as the default for transactions.

**Endpoint:** `PATCH /api/v1/payment-methods/{methodId}/default`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodId | UUID | Yes | Payment method's unique identifier |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Payment method not found

---

### 5. Update Payment Method Priority

Updates the priority order of a payment method.

**Endpoint:** `PATCH /api/v1/payment-methods/{methodId}/priority`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| methodId | UUID | Yes | Payment method's unique identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| priority | integer | Yes | Priority level (lower number = higher priority) |

**Request Headers:**
| Header | Required |
|--------|----------|
| Authorization | Yes |

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` - Invalid priority value
- `401 Unauthorized` - Missing or invalid token
- `404 Not Found` - Payment method not found

---

## Password Reset Token Service APIs

**Base URL:** `/api/v1/users/{userId}/password-reset-token`

### 1. Create Password Reset Token

Generates a password reset token and sends it to the user's email.

**Endpoint:** `POST /api/v1/users/{userId}/password-reset-token/create`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` - User not found
- `500 Internal Server Error` - Email sending failed

---

### 2. Validate Password Reset Token

Validates whether a password reset token is valid and not expired.

**Endpoint:** `POST /api/v1/users/{userId}/password-reset-token/validate`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | string | Yes | Password reset token (from email) |

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` - Token missing
- `401 Unauthorized` - Token invalid or expired
- `404 Not Found` - User not found

---

## Payment Preference Service APIs

**Base URL:** `/api/v1/payment-preferences`

### 1. Get Payment Preference

Retrieves the authenticated user's payment preference (preferred payment method).

**Endpoint:** `GET /api/v1/payment-preferences/{userId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Response:** `200 OK`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "preferredType": "CARD",
  "preferredCardId": "660e8400-e29b-41d4-a716-446655440001"
}
```

**Response (wallet preferred):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "preferredType": "WALLET",
  "preferredCardId": null
}
```

**Error Responses:**
- `404 Not Found` - User preference not found

---

### 2. Set Wallet as Preferred

Sets the authenticated user's wallet as their preferred payment method for fuel purchases.

**Endpoint:** `POST /api/v1/payment-preferences/{userId}/wallet`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "preferredType": "WALLET",
  "preferredCardId": null,
  "createdAt": "2025-05-01T10:30:00Z",
  "updatedAt": "2025-05-01T10:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid request
- `404 Not Found` - User not found

---

### 3. Set Card as Preferred

Sets a specific payment card as the authenticated user's preferred payment method.

**Endpoint:** `POST /api/v1/payment-preferences/{userId}/card/{cardId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |
| cardId | UUID | Yes | Payment card's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "preferredType": "CARD",
  "preferredCardId": "660e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2025-05-01T10:30:00Z",
  "updatedAt": "2025-05-01T10:30:00Z"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid card ID
- `404 Not Found` - User or card not found
- `409 Conflict` - Card does not belong to user

---

## Internal Services

The following services are internal-only and available for service-to-service communication:

### Payment Service

**Base URL:** `/internal/payments` (Internal Service Only)

The Payment Service handles payment processing and eligibility checks for fuel purchases. It integrates with external payment gateways and is called by the Fuel Session Service.

**Endpoints:**

1. **Process Payment**
   ```
   POST /internal/payments/users/{userId}
   ```
   Process a fuel purchase payment for a user.
   
   Request: `{ "amount": 45.50, "orderId": "order-uuid" }`
   Response: `{ "transactionId": "uuid", "status": "PENDING", "amount": 45.50 }`

2. **Check Payment Eligibility**
   ```
   GET /internal/payments/users/{userId}/eligibility?estimatedAmount=500
   ```
   Check if a user can make a payment of a specific amount.
   
   Response: `{ "eligible": true, "reason": "Sufficient wallet balance" }`

For complete Payment Service documentation, see [Payment Service README](payment/README.md).

---

## Status Codes & Error Handling

### Standard HTTP Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| 200 | OK | Successful GET or POST request |
| 201 | Created | Resource successfully created (POST) |
| 204 | No Content | Successful update/delete with no response body |
| 400 | Bad Request | Invalid input or malformed request |
| 401 | Unauthorized | Missing or invalid authentication token |
| 404 | Not Found | Requested resource does not exist |
| 409 | Conflict | Resource conflict (duplicate, constraint violation) |
| 500 | Server Error | Unexpected server error |

### Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2025-05-01T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Field validation failed",
  "path": "/api/v1/users/invalid-uuid"
}
```

---

## Common Data Types

### UUID
Universally Unique Identifier (version 4)
```
Example: 550e8400-e29b-41d4-a716-446655440000
Format: 8-4-4-4-12 hexadecimal digits
```

### DateTime (ISO 8601)
```
Example: 2025-05-01T10:30:00Z
Format: YYYY-MM-DDTHH:mm:ssZ
```

### Currency
```
Type: BigDecimal (precise decimal number)
Example: 45.50 (2 decimal places for cents)
Currency codes: USD, EUR, GBP, etc.
```

### Enumerations

#### StatusType (Wallet)
- `ACTIVE` - Wallet is active
- `INACTIVE` - Wallet is inactive
- `BLOCKED` - Wallet is blocked (restricted operations)

#### FuelType
- `PETROL` - Regular petrol/gasoline
- `DIESEL` - Diesel fuel
- `PREMIUM` - Premium/high-octane fuel

#### TransactionType (Wallet)
- `TOP_UP` - Add funds
- `WITHDRAW` - Withdraw funds
- `REFUND` - Refund transaction
- `BONUS` - Bonus credit

#### TransactionStatusType (Transaction)
- `SUCCESS` - Transaction completed successfully
- `PENDING` - Transaction pending completion
- `FAILED` - Transaction failed

#### PaymentMethodType
- `CREDIT_CARD` - Credit card
- `DEBIT_CARD` - Debit card
- `DIGITAL_WALLET` - Digital wallet (e.g., PayPal)

#### VehicleStatusType
- `ACTIVE` - Vehicle is active
- `INACTIVE` - Vehicle is inactive

---

## Rate Limiting

Currently, the API does not enforce rate limiting. However, clients should implement reasonable request patterns to avoid overloading the system.

**Recommended best practices:**
- Cache frequently accessed data
- Batch requests when possible
- Implement exponential backoff for retries

---

## API Versioning

This documentation covers **v1** of the API. Future versions will be available at:
- `/api/v2/...`
- `/api/v3/...`

Legacy versions will be supported for a deprecation period.

---

## Support & Contact

For API support, issues, or feature requests:
- **Email:** api-support@smartfuel.com
- **Documentation:** https://docs.smartfuel.com
- **GitHub Issues:** https://github.com/smartfuel/api/issues

---

**Last Updated:** May 1, 2026  
**Document Version:** 1.0.0
