# Smart Fuel Management System — Admin API Reference

> **Version:** 1.0.0  
> **Base URL:** `http://localhost:8080` (API Gateway)  
> **Authentication:** All `/api/v1/admin/**` endpoints require a JWT with the `ADMIN` role via `Authorization: Bearer <token>` header.  
> **Coverage:** Admin APIs across **Auth**, **User**, **Vehicle**, **Station**, **Transaction**, **Dashboard**, and **Fuel-Session** modules.

---

## Table of Contents

1. [Auth Service Admin APIs](#1-auth-service-admin-apis)
2. [User Service Admin APIs](#2-user-service-admin-apis)
3. [Vehicle Service Admin APIs](#3-vehicle-service-admin-apis)
4. [Station Service Admin APIs](#4-station-service-admin-apis)
5. [Transaction Service Admin APIs](#5-transaction-service-admin-apis)
6. [Dashboard Service](#6-dashboard-service)
7. [Fuel-Session Service](#7-fuel-session-service)
8. [Common Enums](#8-common-enums)
9. [Pagination](#9-pagination)
10. [Error Handling](#10-error-handling)

---

## Authentication

Admin endpoints require the `ADMIN` role authority.

**Request Header:**
```
Authorization: Bearer <admin_jwt_token>
```

**Getting an Admin Token:** `POST /api/v1/auth/admin/login` (see [Auth Service Admin APIs](#1-auth-service-admin-apis))

---

## 1. Auth Service Admin APIs

**Base URL:** `/api/v1/admin/auth`

### 1.1 Admin Login

Obtains a JWT token for an admin account.

**Endpoint:** `POST /api/v1/auth/admin/login`

**Request Body:**
```json
{
  "type": "EMAIL",
  "identifier": "admin@example.com",
  "password": "SecurePassword123!"
}
```
- `type` — `LoginMethodType`: `EMAIL` or `PHONE`
- `identifier` — the email or phone number (E.164 format `+1234567890` for phone)
- `password` — minimum 8 characters

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `400 Bad Request` — Invalid input
- `401 Unauthorized` — Invalid credentials

---

### 1.2 Register Station Employee / Station Account

Creates a new fuel station employee account.

**Endpoint:** `POST /api/v1/admin/auth/station-accounts`

**Request Body:**
```json
{
  "email": "employee@station.com",
  "fullName": "John Station Employee",
  "phoneNumber": "+1234567890",
  "password": "SecurePassword123!",
  "stationId": "550e8400-e29b-41d4-a716-446655440000"
}
```
**Field Rules:**
- `email` — valid email, max 254 chars
- `fullName` — 2–100 chars
- `phoneNumber` — E.164 format `^\+[1-9]\d{7,14}$`
- `password` — 8–72 chars
- `stationId` — UUID of the station

**Response:** `201 Created` (No body)

**Error Responses:**
- `400 Bad Request` — Validation failed
- `409 Conflict` — Email/phone already registered

---

### 1.3 List Station Accounts

Returns all station employee accounts.

**Endpoint:** `GET /api/v1/admin/auth/station-accounts`

**Request Headers:**
| Header | Required | Description |
|--------|----------|-------------|
| Authorization | Yes | Bearer token (ADMIN role) |

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Station Employee",
    "email": "employee@station.com",
    "phoneNumber": "+1234567890",
    "statusType": "ACTIVE",
    "createdAt": "2026-08-28T12:00:00",
    "updatedAt": "2026-08-28T12:00:00"
  }
]
```

**Response Fields:**
- `id` — UUID
- `fullName` — string
- `email` — string
- `phoneNumber` — string
- `statusType` — `AccountStatusType`: `ACTIVE`, `INACTIVE`, `PENDING`, `BLOCKED`, `SUSPENDED`
- `createdAt` — ISO 8601 datetime
- `updatedAt` — ISO 8601 datetime

---

### 1.4 Search Station Accounts

Searches and filters station employee accounts with pagination.

**Endpoint:** `GET /api/v1/admin/auth/station-accounts`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| searchType | AccountSearchType | Yes | `NAME`, `EMAIL`, or `PHONE` |
| search | string | Yes | Search term |
| status | AccountStatusType | No | Filter by status |
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |
| sort | string | No | Sort property/order |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Station Employee",
      "email": "employee@station.com",
      "phoneNumber": "+1234567890",
      "statusType": "ACTIVE",
      "createdAt": "2026-08-28T12:00:00",
      "updatedAt": "2026-08-28T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "unsorted": true }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

### 1.5 Get Employee Details

Returns detailed information for a specific station employee.

**Endpoint:** `GET /api/v1/admin/auth/employees/{employeeId}/details`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| employeeId | UUID | Yes | Employee's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "John Station Employee",
  "email": "employee@station.com",
  "phoneNumber": "+1234567890",
  "statusType": "ACTIVE",
  "createdAt": "2026-08-28T12:00:00",
  "updatedAt": "2026-08-28T12:00:00",
  "station": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Shell Station Downtown",
    "city": "Istanbul",
    "address": "123 Main Street"
  }
}
```

**Error Responses:**
- `404 Not Found` — Employee not found

---

### 1.6 Update Station Employee

Updates an employee's password and/or account status.

**Endpoint:** `PATCH /api/v1/admin/auth/station-accounts/{employeeId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| employeeId | UUID | Yes | Employee's unique identifier |

**Request Body:** (both fields optional, at least one required)
```json
{
  "password": "NewSecurePassword123!",
  "accountStatusType": "ACTIVE"
}
```
- `password` — string (8–72 chars)
- `accountStatusType` — `AccountStatusType`: `ACTIVE`, `INACTIVE`, `PENDING`, `BLOCKED`, `SUSPENDED`

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid input
- `404 Not Found` — Employee not found

---

### 1.7 Change Employee Identifier (Email/Phone)

Initiates an identifier change (email or phone) for a station employee. An OTP is sent for verification.

**Endpoint:** `POST /api/v1/admin/auth/{employeeId}/identifier-change`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| employeeId | UUID | Yes | Employee's unique identifier |

**Request Body:**
```json
{
  "type": "EMAIL",
  "identifier": "new.email@example.com"
}
```
- `type` — `OtpType`: `EMAIL` or `PHONE`
- `identifier` — new email address or phone number in E.164 format

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid input
- `404 Not Found` — Employee not found
- `409 Conflict` — Identifier already in use

---

### 1.8 Verify Identifier Change OTP

Verifies the OTP sent during an identifier change.

**Endpoint:** `POST /api/v1/admin/auth/identifier-change/{changeId}/verify`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| changeId | UUID | Yes | Identifier-change record ID |

**Request Body:**
```json
{
  "otp": "123456"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid or expired OTP
- `404 Not Found` — Change record not found

---

### 1.9 Resend Identifier Change OTP

Resends the OTP for an identifier change.

**Endpoint:** `POST /api/v1/admin/auth/identifier-change/{changeId}/resend`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| changeId | UUID | Yes | Identifier-change record ID |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` — Change record not found

---

## 2. User Service Admin APIs

**Base URL:** `/api/v1/admin/users`

### 2.1 List All Users

Returns a paginated list of all registered users.

**Endpoint:** `GET /api/v1/admin/users`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |
| sort | string | No | Sort property/order |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "+1234567890",
      "statusType": "ACTIVE",
      "createdAt": "2026-08-28T12:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "unsorted": true }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

### 2.2 Search / Filter Users

Searches and filters users by various criteria with pagination.

**Endpoint:** `GET /api/v1/admin/users`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| searchType | AccountSearchType | Yes | `NAME`, `EMAIL`, `PHONE`, `ROLE`, or `ACCOUNT_STATUS_TYPE` |
| search | string | No | Search term (required unless filtering by status/role) |
| status | AccountStatusType | No | Filter by account status |
| role | RoleType | No | Filter by role |
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |

**Response:** `200 OK` — Same paginated `UserSummaryResponse` structure as [2.1](#21-list-all-users).

**Response Fields per item:**
- `id` — UUID
- `fullName` — string
- `email` — string
- `phoneNumber` — string
- `statusType` — `AccountStatusType`
- `createdAt` — ISO 8601 datetime

---

### 2.3 Update User (Full Name)

Updates a user's profile information.

**Endpoint:** `PATCH /api/v1/admin/users/{userId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Request Body:**
```json
{
  "fullName": "Johnathan Doe"
}
```

> **Note:** The authenticated admin's ID (from the JWT) is used to perform the update via `Authentication` principal, not the path variable.

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid input
- `404 Not Found` — User not found

---

## 3. Vehicle Service Admin APIs

**Base URL:** `/api/v1/admin/vehicles`

### 3.1 Change Vehicle Status

Updates the status of a vehicle.

**Endpoint:** `PATCH /api/v1/admin/vehicles/{vehicleId}/status`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Request Body:**
```json
{
  "status": "BLOCKED"
}
```
- `status` — `VehicleStatusType`: `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING`

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid status
- `404 Not Found` — Vehicle not found

---

### 3.2 Delete Vehicle

Permanently deletes a vehicle record.

**Endpoint:** `DELETE /api/v1/admin/vehicles/{vehicleId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` — Vehicle not found

---

### 3.3 Find Vehicle by Plate Number

Retrieves a vehicle by its plate number.

**Endpoint:** `GET /api/v1/admin/vehicles/plates/{plateNumber}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| plateNumber | string | Yes | Vehicle plate number (e.g., `ABC-1234`) |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "plateNumber": "ABC-1234",
  "rfidTag": "RFID-001",
  "brand": "Toyota",
  "model": "Camry",
  "year": 2022,
  "fuelType": "GASOLINE",
  "status": "ACTIVE",
  "createdAt": "2026-08-28T12:00:00",
  "updatedAt": "2026-08-28T12:00:00"
}
```

**Response Fields:**
- `id` — UUID
- `plateNumber` — string
- `rfidTag` — string or null
- `brand` — string
- `model` — string
- `year` — int
- `fuelType` — `FuelType`: `GASOLINE`, `DIESEL`, `HYBRID`, `ELECTRIC`
- `status` — `VehicleStatusType`: `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING`
- `createdAt` / `updatedAt` — ISO 8601 datetime

**Error Responses:**
- `404 Not Found` — Vehicle not found

---

### 3.4 Get User Vehicles

Retrieves all vehicles belonging to a specific user.

**Endpoint:** `GET /api/v1/admin/vehicles/users/{userId}`
**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Response:** `200 OK`
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "plateNumber": "ABC-1234",
    "rfidTag": "RFID-001",
    "brand": "Toyota",
    "model": "Camry",
    "year": 2022,
    "fuelType": "GASOLINE",
    "status": "ACTIVE",
    "createdAt": "2026-08-28T12:00:00",
    "updatedAt": "2026-08-28T12:00:00"
  }
]
```

**Error Responses:**
- `404 Not Found` — User not found

---

### 3.5 Assign RFID to Vehicle

Assigns an RFID tag to a vehicle by vehicle ID.

**Endpoint:** `POST /api/v1/admin/vehicles/rfid/assign`

**Request Body:**
```json
{
  "vehicleId": "550e8400-e29b-41d4-a716-446655440000",
  "rfid": "RFID-001"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid input
- `404 Not Found` — Vehicle not found
- `409 Conflict` — RFID already assigned to another vehicle

---

### 3.6 Find Vehicle by RFID

Retrieves a vehicle by its RFID tag.

**Endpoint:** `GET /api/v1/admin/vehicles/rfid/{rfid}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| rfid | string | Yes | RFID tag value |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "plateNumber": "ABC-1234",
  "brand": "Toyota",
  "model": "Camry",
  "year": 2022,
  "tankCapacity": 55.00,
  "fuelType": "GASOLINE",
  "rfidTag": "RFID-001",
  "status": "ACTIVE",
  "userId": "660e8400-e29b-41d4-a716-446655440001",
  "createdAt": "2026-08-28T12:00:00",
  "updatedAt": "2026-08-28T12:00:00"
}
```

> **Note:** This endpoint returns the raw `Vehicle` entity including `tankCapacity` and `userId`.

**Error Responses:**
- `404 Not Found` — No vehicle with that RFID

---

### 3.7 Remove RFID from Vehicle

Removes the RFID tag from a vehicle.

**Endpoint:** `DELETE /api/v1/admin/vehicles/rfid/{vehicleId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` — Vehicle not found

---

## 4. Station Service Admin APIs

**Base URL:** `/api/v1/admin/stations`

### 4.1 Create Station

Creates a new fuel station.

**Endpoint:** `POST /api/v1/admin/stations`

**Request Body:**
```json
{
  "name": "Shell Station Downtown",
  "city": "Istanbul",
  "address": "123 Main Street",
  "contactInformation": "+902123456789",
  "latitude": 41.0082,
  "longitude": 28.9784
}
```

**Response:** `201 Created` (No body)

**Error Responses:**
- `400 Bad Request` — Validation failed or missing required fields
- `409 Conflict` — Station with the same name/address already exists

---

### 4.2 Get Station Details

Retrieves full details of a station including stats, account, fuel prices, and pumps.

**Endpoint:** `GET /api/v1/admin/stations/{stationId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Shell Station Downtown",
  "city": "Istanbul",
  "address": "123 Main Street",
  "contactInformation": "+902123456789",
  "latitude": 41.0082,
  "longitude": 28.9784,
  "status": "ACTIVE",
  "createdAt": "2026-08-28T12:00:00",
  "updatedAt": "2026-08-28T12:00:00",
  "transactionStats": {
    "transactionsCount": 1250,
    "vehiclesCount": 340
  },
  "stationAccount": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "fullName": "John Station Employee",
    "email": "employee@station.com",
    "phoneNumber": "+1234567890",
    "status": "ACTIVE",
    "createdAt": "2026-08-28T12:00:00",
    "updatedAt": "2026-08-28T12:00:00"
  },
  "fuelPrices": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "fuelType": "GASOLINE",
      "price": 42.50,
      "createdAt": "2026-08-28T12:00:00"
    }
  ],
  "pumps": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440003",
      "pumpNumber": 1,
      "status": "ACTIVE",
      "fuelTypes": ["GASOLINE", "DIESEL"]
    }
  ]
}
```

**Response Fields:**
- `status` — `StationStatusType`: `ACTIVE`, `INACTIVE`, `MAINTENANCE`
- `stationAccount.status` — `AccountStatusType`

**Error Responses:**
- `404 Not Found` — Station not found

---

### 4.3 Update Station

Updates station information and/or status.

**Endpoint:** `PATCH /api/v1/admin/stations/{stationId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |

**Request Body:** (all fields optional)
```json
{
  "name": "Shell Station Uptown",
  "city": "Ankara",
  "address": "456 Another Street",
  "contactInformation": "+903123456789",
  "latitude": 39.9334,
  "longitude": 32.8597,
  "status": "MAINTENANCE"
}
```

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request` — Invalid input
- `404 Not Found` — Station not found

---

### 4.4 List / Search Stations

Returns a paginated list of stations, optionally filtered by search term and status.

**Endpoint:** `GET /api/v1/admin/stations`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| search | string | No | Search by name/city/address |
| status | StationStatusType | No | Filter by status |
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20, **must be 20–50**) |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Shell Station Downtown",
      "city": "Istanbul",
      "address": "123 Main Street",
      "status": "ACTIVE",
      "transactionStats": {
        "transactionsCount": 1250,
        "vehiclesCount": 340
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "unsorted": false }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0,
  "empty": false
}
```

**Error Responses:**
- `400 Bad Request` — Page size must be between 20 and 50

---

### 4.5 Create Pump

Creates a new pump for a station.

**Endpoint:** `POST /api/v1/admin/stations/{stationId}/pumps`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |

**Request Body:**
```json
{
  "pumpNumber": 3,
  "fuelTypes": ["GASOLINE", "DIESEL"]
}
```
- `fuelTypes` — non-empty set of `FuelType`: `GASOLINE`, `DIESEL`, `HYBRID`, `ELECTRIC`

**Response:** `201 Created`
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "pumpNumber": 3,
  "status": "ACTIVE",
  "fuelTypes": ["GASOLINE", "DIESEL"]
}
```
- `status` — `PumpStatusType`: `ACTIVE`, `INACTIVE`

**Error Responses:**
- `400 Bad Request` — Validation failed
- `404 Not Found` — Station not found
- `409 Conflict` — Pump number already exists

---

### 4.6 Create Fuel Price

Sets a new price for a fuel type at a station.

**Endpoint:** `POST /api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |
| fuelType | FuelType | Yes | `GASOLINE`, `DIESEL`, `HYBRID`, or `ELECTRIC` |

**Request Body:**
```json
{
  "price": 45.75
}
```
- `price` — must be a positive number

**Response:** `201 Created` (No body)

**Error Responses:**
- `400 Bad Request` — Price must be positive
- `404 Not Found` — Station not found

---

### 4.7 Get Fuel Price History

Retrieves all historical fuel prices for a station.

**Endpoint:** `GET /api/v1/admin/stations/{stationId}/fuel-prices/history`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |

**Response:** `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "fuelType": "GASOLINE",
    "price": 42.50,
    "createdAt": "2026-08-28T12:00:00"
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "fuelType": "GASOLINE",
    "price": 45.75,
    "createdAt": "2026-08-29T12:00:00"
  }
]
```

**Error Responses:**
- `404 Not Found` — Station not found

---

### 4.8 Search / Filter Station Fuel Prices

Returns a paginated list of fuel prices for a station, optionally filtered by fuel type and status.

**Endpoint:** `GET /api/v1/admin/stations/{stationId}/fuel-prices`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| stationId | UUID | Yes | Station's unique identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| fuelType | FuelType | No | Filter by fuel type |
| status | FuelPriceStatusType | No | `ACTIVE` or `INACTIVE` |
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |
| sort | string | No | Sort property/order |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "fuelType": "GASOLINE",
      "price": 45.75,
      "createdAt": "2026-08-29T12:00:00"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

### 4.9 Change Station Employee's Station

Moves a station employee to a different station.

**Endpoint:** `PATCH /api/v1/admin/station-employees/{employeeId}/stations/{stationId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| employeeId | UUID | Yes | Employee's unique identifier |
| stationId | UUID | Yes | Target station's unique identifier |

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` — Employee or station not found

---

## 5. Transaction Service Admin APIs

**Base URL:** `/api/v1/admin/transactions`

### 5.1 Get Transaction Statistics

Returns aggregated transaction statistics for a date range, optionally filtered by stations.

**Endpoint:** `GET /api/v1/admin/transactions/statistics`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| from | LocalDate | Yes | Start date (e.g., `2026-08-01`) |
| to | LocalDate | Yes | End date (e.g., `2026-08-31`) |
| stationIds | List<UUID> | No | Comma-separated station IDs |

**Response:** `200 OK`
```json
{
  "transactionCount": 1250,
  "fuelVolume": 52500.50,
  "revenue": 2387437.50,
  "fuelSalesByType": [
    {
      "fuelType": "GASOLINE",
      "fuelVolume": 32000.25,
      "revenue": 1440011.25
    },
    {
      "fuelType": "DIESEL",
      "fuelVolume": 20500.25,
      "revenue": 947426.25
    }
  ]
}
```

**Error Responses:**
- `400 Bad Request` — Invalid date range

---

### 5.2 List All Transactions

Returns a paginated list of all transactions.

**Endpoint:** `GET /api/v1/admin/transactions`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |
| sort | string | No | Sort property/order |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "pumpId": "880e8400-e29b-41d4-a716-446655440003",
      "fuelType": "GASOLINE",
      "liters": 40.25,
      "pricePerLiter": 45.75,
      "amount": 1841.44,
      "createdAt": "2026-08-28T12:00:00",
      "status": "SUCCESS",
      "vehicle": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "plateNumber": "ABC-1234",
        "brand": "Toyota",
        "model": "Camry",
        "year": 2022
      },
      "station": {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "name": "Shell Station Downtown",
        "city": "Istanbul",
        "address": "123 Main Street"
      }
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1,
  "size": 20,
  "number": 0,
  "empty": false
}
```

**Response Fields:**
- `status` — `PaymentStatusType`: `SUCCESS`, `FAILED`, `PENDING`

---

### 5.3 Get Transaction by ID

Retrieves a specific transaction's details.

**Endpoint:** `GET /api/v1/admin/transactions/{transactionId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| transactionId | UUID | Yes | Transaction's unique identifier |

**Response:** `200 OK` — Same structure as an item in [5.2](#52-list-all-transactions).

**Error Responses:**
- `404 Not Found` — Transaction not found

---

### 5.4 Get User Transactions

Returns a paginated list of all transactions for a specific user.

**Endpoint:** `GET /api/v1/admin/transactions/users/{userId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | User's unique identifier |

**Query Parameters:** Standard pagination (`page`, `size`, `sort`)

**Response:** `200 OK` — Paginated `TransactionResponse` (same structure as [5.2](#52-list-all-transactions)).

**Error Responses:**
- `404 Not Found` — User not found

---

### 5.5 Get Transactions by Date Range

Returns a paginated list of transactions within a date/time range.

**Endpoint:** `GET /api/v1/admin/transactions/search`

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| from | LocalDateTime | Yes | Start datetime (e.g., `2026-08-01T00:00:00`) |
| to | LocalDateTime | Yes | End datetime (e.g., `2026-08-31T23:59:59`) |
| page | int | No | Page number (default 0) |
| size | int | No | Page size (default 20) |

**Response:** `200 OK` — Paginated `TransactionResponse` (same structure as [5.2](#52-list-all-transactions)).

---

### 5.6 Get Vehicle Transaction History

Returns transaction history (summary) for a specific vehicle.

**Endpoint:** `GET /api/v1/admin/transactions/{vehicleId}`

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| vehicleId | UUID | Yes | Vehicle's unique identifier |

**Response:** `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "amount": 1841.44,
    "fuelType": "GASOLINE",
    "createdAt": "2026-08-28T12:00:00"
  },
  {
    "id": "990e8400-e29b-41d4-a716-446655440004",
    "amount": 1200.00,
    "fuelType": "DIESEL",
    "createdAt": "2026-08-20T14:30:00"
  }
]
```

**Error Responses:**
- `404 Not Found` — Vehicle not found

---

## 6. Dashboard Service

**Base URL:** `/api/v1/dashboard`

### 6.1 Overview

The Dashboard module exposes a **user-facing** endpoint only:

- `GET /api/v1/dashboard` — Returns `{ "vehiclesCount": 2, "transactionsCount": 5 }` for the authenticated user.

### 6.2 Admin APIs

**None.** The Dashboard module does **not** expose any `/api/v1/admin/**` endpoints. Admin dashboard data is sourced from the **Transaction Service** (`GET /api/v1/admin/transactions/statistics`) and **Station Service** (`GET /api/v1/admin/stations`).

---

## 7. Fuel-Session Service

### 7.1 Overview

The Fuel-Session module is **MQTT-based** and does **not** expose any REST controllers or `/api/v1/admin/**` endpoints. It communicates via the MQTT broker for session lifecycle (start/stop) and calls Payment/Station services internally.

### 7.2 Admin APIs

**None.**

---

## 8. Common Enums

| Enum | Values | Used In |
|------|--------|---------|
| `AccountStatusType` | `ACTIVE`, `INACTIVE`, `PENDING`, `BLOCKED`, `SUSPENDED` | Auth, User, Station modules |
| `AccountSearchType` | `NAME`, `EMAIL`, `PHONE` (Auth) / `NAME`, `EMAIL`, `PHONE`, `ROLE`, `ACCOUNT_STATUS_TYPE` (User) | Auth, User modules |
| `LoginMethodType` | `EMAIL`, `PHONE` | Auth module |
| `OtpType` | `EMAIL`, `PHONE` | Auth module |
| `RoleType` | `USER`, `ADMIN`, `STATION` | User module |
| `FuelType` | `GASOLINE`, `DIESEL`, `HYBRID`, `ELECTRIC` | Vehicle, Station, Transaction modules |
| `VehicleStatusType` | `ACTIVE`, `INACTIVE`, `BLOCKED`, `PENDING` | Vehicle module |
| `StationStatusType` | `ACTIVE`, `INACTIVE`, `MAINTENANCE` | Station module |
| `FuelPriceStatusType` | `ACTIVE`, `INACTIVE` | Station module |
| `PumpStatusType` | `ACTIVE`, `INACTIVE` | Station module |
| `PaymentStatusType` | `SUCCESS`, `FAILED`, `PENDING` | Transaction module |

---

## 9. Pagination

All list/search endpoints return Spring Data `Page<T>` objects with the following structure:

```json
{
  "content": [ ... ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": false, "unsorted": true },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "sort": { "sorted": false, "unsorted": true },
  "empty": false
}
```

**Pagination Parameters** (as query parameters):
- `page` — zero-based page index (default `0`)
- `size` — page size (default `20`)
- `sort` — e.g., `sort=createdAt,desc` (repeatable)

> **Note:** The Station **list/search** endpoint (`GET /api/v1/admin/stations`) enforces `size` between **20 and 50**.

---

## 10. Error Handling

All services return a consistent error format:

```json
{
  "timestamp": "2026-08-28T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable error message",
  "path": "/api/v1/admin/stations"
}
```

### Common HTTP Status Codes

| Status | Description |
|--------|-------------|
| `200 OK` | Request succeeded, response body returned |
| `201 Created` | Resource created successfully |
| `204 No Content` | Request succeeded, no response body |
| `400 Bad Request` | Invalid input, validation failure, or missing parameters |
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Authenticated user does not have `ADMIN` role |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Duplicate resource or conflicting state |

---

## Quick Endpoint Summary

| Method | Endpoint | Description | Module |
|--------|----------|-------------|--------|
| POST | `/api/v1/auth/admin/login` | Admin login (JWT) | Auth |
| POST | `/api/v1/admin/auth/station-accounts` | Register station employee | Auth |
| GET | `/api/v1/admin/auth/station-accounts` | List station accounts | Auth |
| GET | `/api/v1/admin/auth/station-accounts` | Search station accounts | Auth |
| GET | `/api/v1/admin/auth/employees/{employeeId}/details` | Employee details | Auth |
| PATCH | `/api/v1/admin/auth/station-accounts/{employeeId}` | Update employee | Auth |
| POST | `/api/v1/admin/auth/{employeeId}/identifier-change` | Change identifier | Auth |
| POST | `/api/v1/admin/auth/identifier-change/{changeId}/verify` | Verify OTP | Auth |
| POST | `/api/v1/admin/auth/identifier-change/{changeId}/resend` | Resend OTP | Auth |
| GET | `/api/v1/admin/users` | List/search users | User |
| PATCH | `/api/v1/admin/users/{userId}` | Update user | User |
| PATCH | `/api/v1/admin/vehicles/{vehicleId}/status` | Change vehicle status | Vehicle |
| DELETE | `/api/v1/admin/vehicles/{vehicleId}` | Delete vehicle | Vehicle |
| GET | `/api/v1/admin/vehicles/plates/{plateNumber}` | Find by plate | Vehicle |
| GET | `/api/v1/admin/vehicles/users/{userId}` | User vehicles | Vehicle |
| POST | `/api/v1/admin/vehicles/rfid/assign` | Assign RFID | Vehicle |
| GET | `/api/v1/admin/vehicles/rfid/{rfid}` | Find by RFID | Vehicle |
| DELETE | `/api/v1/admin/vehicles/rfid/{vehicleId}` | Remove RFID | Vehicle |
| POST | `/api/v1/admin/stations` | Create station | Station |
| GET | `/api/v1/admin/stations/{stationId}` | Station details | Station |
| PATCH | `/api/v1/admin/stations/{stationId}` | Update station | Station |
| GET | `/api/v1/admin/stations` | List/search stations | Station |
| POST | `/api/v1/admin/stations/{stationId}/pumps` | Create pump | Station |
| POST | `/api/v1/admin/stations/{stationId}/fuel-prices/{fuelType}` | Create fuel price | Station |
| GET | `/api/v1/admin/stations/{stationId}/fuel-prices/history` | Fuel price history | Station |
| GET | `/api/v1/admin/stations/{stationId}/fuel-prices` | Search fuel prices | Station |
| PATCH | `/api/v1/admin/station-employees/{employeeId}/stations/{stationId}` | Change employee's station | Station |
| GET | `/api/v1/admin/transactions/statistics` | Transaction statistics | Transaction |
| GET | `/api/v1/admin/transactions` | List all transactions | Transaction |
| GET | `/api/v1/admin/transactions/{transactionId}` | Get transaction | Transaction |
| GET | `/api/v1/admin/transactions/users/{userId}` | User transactions | Transaction |
| GET | `/api/v1/admin/transactions/search` | Search by date range | Transaction |
| GET | `/api/v1/admin/transactions/{vehicleId}` | Vehicle transaction history | Transaction |