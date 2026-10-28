# Customer Profile Service

Backend service for Northwind Rewards customer accounts: registration, authentication,
profile management and loyalty points.

## Tech stack

- Java 17, Spring Boot 3.3
- Spring Data JPA / Hibernate
- PostgreSQL
- JWT (jjwt)
- JUnit 5, Mockito

## Running locally

### Docker Compose

```bash
docker compose up --build
```

The service starts on `http://localhost:8080`, PostgreSQL on `localhost:5432`.

### From IDE

Start only the database:

```bash
docker compose up db
```

Then run `CustomerProfileApplication` (or `mvn spring-boot:run`).

### Tests

```bash
mvn test
```

## API

### Auth

| Method | Path                        | Description                     |
|--------|-----------------------------|---------------------------------|
| POST   | `/api/auth/register`        | Register a new customer         |
| POST   | `/api/auth/login`           | Log in, returns JWT             |
| POST   | `/api/auth/logout`          | Invalidate current session      |
| POST   | `/api/auth/forgot-password` | Send password reset code        |
| POST   | `/api/auth/reset-password`  | Set new password using the code |

### Customers (requires `Authorization: Bearer <token>`)

| Method | Path                               | Description                 |
|--------|------------------------------------|-----------------------------|
| GET    | `/api/customers/me`                | Current customer profile    |
| PUT    | `/api/customers/me`                | Update profile              |
| POST   | `/api/customers/me/password`       | Change password             |
| POST   | `/api/customers/me/points/transfer`| Transfer loyalty points     |
| GET    | `/api/customers`                   | List customers              |
| GET    | `/api/customers/{id}`              | Get customer by id          |
| GET    | `/api/customers/search?q=`         | Search customers            |
| GET    | `/api/customers/lookup?email=`     | Find customer by email      |
| DELETE | `/api/customers/{id}`              | Delete customer             |

### Admin (requires `X-Api-Key` header)

| Method | Path                               | Description                 |
|--------|------------------------------------|-----------------------------|
| GET    | `/api/admin/customers`             | All customers               |
| POST   | `/api/admin/customers/import`      | Bulk import                 |
| POST   | `/api/admin/customers/{id}/block`  | Block customer              |
| GET    | `/api/admin/stats`                 | Service statistics          |

## Example

```bash
curl -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"anna@example.com","password":"Secret123","firstName":"Anna","lastName":"Smith"}'

curl -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"anna@example.com","password":"Secret123"}'
```
