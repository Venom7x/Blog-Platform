# Blogging Platform Backend

A production-ready backend for a Medium/Dev.to-style blogging platform, built with **Java 17, Spring Boot 3, Spring Security 6, Spring Data JPA/Hibernate, MySQL 8, and JWT authentication**.

## Architecture

Clean, layered architecture with a strict one-way dependency flow:

```
Controller (REST, validation, HTTP concerns)
     ↓
Service interface + impl (business rules, transactions, ownership checks)
     ↓
Repository (Spring Data JPA)
     ↓
Entity (JPA / Hibernate, MySQL)
```

- **DTOs at every boundary** — entities are never serialized directly to JSON. Request DTOs carry `@Valid` bean-validation constraints; Response DTOs are hand-mapped via a small `mapper` package.
- **Stateless JWT auth** — access token (15 min) + refresh token (7 days), HMAC-SHA256 signed, validated in a single `OncePerRequestFilter`.
- **Role-based access control** — `USER` / `ADMIN` via Spring Security `@PreAuthorize` + URL-matcher rules, plus explicit **ownership checks** in the service layer (a user can only edit/delete *their own* posts/comments; admins bypass this).
- **N+1 mitigation** — `@EntityGraph` on list/detail queries to fetch `author` + `category` in one query; `hibernate.default_batch_fetch_size` as a second line of defense; `open-in-view: false` to keep fetching intentional and inside the service layer.
- **Auditing** — `createdAt`, `updatedAt`, `createdBy`, `modifiedBy` automatically populated via Spring Data JPA Auditing (`BaseEntity` + `AuditorAware`), plus optimistic-locking `@Version`.
- **Global exception handling** — a single `@RestControllerAdvice` maps domain exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `UnauthorizedActionException`, etc.) and `MethodArgumentNotValidException` to consistent JSON error responses with correct HTTP status codes.
- **Duplicate-like prevention** — enforced at both the service layer (existence check) and the database (`UNIQUE (post_id, user_id)` constraint), so it's safe under concurrent requests.

## Folder structure

```
src/main/java/com/blogplatform/
├── BlogPlatformApplication.java
├── config/            # SecurityConfig, AuditConfig, OpenApiConfig
├── security/           # JwtUtil, JwtAuthenticationFilter, CustomUserDetails, entry/access-denied handlers
├── entity/             # BaseEntity, User, Role, Post, Category, Comment, PostLike
├── enums/              # RoleName, PostStatus
├── repository/         # Spring Data JPA repositories (+ @EntityGraph, JPQL search)
├── dto/
│   ├── request/        # RegisterRequest, LoginRequest, PostRequest, CommentRequest, ...
│   └── response/        # UserResponse, PostResponse, PageResponse, ApiResponse, ErrorResponse, ...
├── mapper/             # Entity -> Response DTO mappers
├── service/            # Interfaces
│   └── impl/            # Implementations (@Transactional business logic)
├── controller/         # REST controllers (thin; delegate to services)
└── exception/          # Custom exceptions + GlobalExceptionHandler
```

Also included:
- `docs/schema.sql` — reference relational schema (Hibernate auto-generates this at runtime; this file documents it explicitly)
- `docs/ER-diagram.md` — Mermaid ER diagram + relationship summary
- `docs/API-REFERENCE.md` — full endpoint list with sample JSON requests/responses
- `Dockerfile` + `docker-compose.yml` — containerized MySQL + app

## Running it

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8 running locally (or use Docker Compose below)

### Option A — Docker Compose (MySQL + app)
```bash
docker compose up --build
```
App will be available at `http://localhost:8080`, Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Option B — Run locally against your own MySQL
```bash
export DB_HOST=localhost DB_PORT=3306 DB_NAME=blog_platform DB_USERNAME=root DB_PASSWORD=root
export JWT_SECRET=$(openssl rand -base64 48)   # generate your own 256-bit+ secret
mvn spring-boot:run
```

Hibernate is set to `ddl-auto: update` for convenience in this deliverable — **swap to `validate` and introduce Flyway/Liquibase migrations before real production use.**

### Quick smoke test
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"Passw0rd123","fullName":"John Doe"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"johndoe","password":"Passw0rd123"}'

# Use the accessToken from the response:
curl http://localhost:8080/api/v1/users/me -H "Authorization: Bearer <accessToken>"
```

## Making the first ADMIN user
By design, registration always assigns `ROLE_USER`. To promote a user to admin, either:
1. Manually insert into `user_roles` after inserting a `ROLE_ADMIN` row into `roles`, or
2. Have an existing admin call `PUT /api/v1/admin/users/{userId}/roles` with `{"roles": ["ROLE_USER","ROLE_ADMIN"]}`.

For a fresh install, option 1 (a one-time SQL bootstrap) is simplest:
```sql
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'johndoe' AND r.name = 'ROLE_ADMIN';
```

## Security notes for real production use
- Replace the demo `app.jwt.secret` in `application.yml` with a securely generated secret injected via environment variable / secrets manager — never commit it.
- Switch `ddl-auto` to `validate` and manage schema changes with Flyway or Liquibase.
- Put the API behind HTTPS/TLS termination (load balancer or reverse proxy).
- Consider a token-revocation/blacklist store (e.g. Redis) if you need immediate logout/revoke semantics beyond short-lived access tokens.
- Add rate limiting on `/api/v1/auth/**` to slow down credential stuffing / brute force.
