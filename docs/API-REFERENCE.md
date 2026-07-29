# API Reference

Base URL: `http://localhost:8080`
Interactive docs (Swagger UI): `http://localhost:8080/swagger-ui.html`

All protected endpoints require header: `Authorization: Bearer <accessToken>`

---
## 1. Auth — `/api/v1/auth` (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user (ROLE_USER by default) |
| POST | `/api/v1/auth/login` | Login, returns access + refresh token |
| POST | `/api/v1/auth/refresh` | Exchange a refresh token for a new access token |

**POST /api/v1/auth/register**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Passw0rd123",
  "fullName": "John Doe"
}
```
Response `201`:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresInMs": 900000,
    "user": {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "fullName": "John Doe",
      "roles": ["ROLE_USER"],
      "enabled": true,
      "createdAt": "2026-07-15T10:00:00"
    }
  }
}
```

**POST /api/v1/auth/login**
```json
{ "usernameOrEmail": "johndoe", "password": "Passw0rd123" }
```

**POST /api/v1/auth/refresh**
```json
{ "refreshToken": "eyJhbGciOi..." }
```

---
## 2. Users — `/api/v1/users`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/users/me` | User | View own profile |
| PUT | `/api/v1/users/me` | User | Update own profile |
| PUT | `/api/v1/users/me/password` | User | Change password |
| DELETE | `/api/v1/users/me` | User | Delete own account |
| GET | `/api/v1/users/{username}` | Public | View public profile |

**PUT /api/v1/users/me**
```json
{ "fullName": "John A. Doe", "bio": "Backend engineer.", "avatarUrl": "https://cdn.example.com/a.png" }
```

**PUT /api/v1/users/me/password**
```json
{ "currentPassword": "Passw0rd123", "newPassword": "NewPassw0rd456" }
```

---
## 3. Categories — `/api/v1/categories`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/categories` | Admin | Create category |
| PUT | `/api/v1/categories/{id}` | Admin | Update category |
| DELETE | `/api/v1/categories/{id}` | Admin | Delete category |
| GET | `/api/v1/categories/{id}` | Public | Get one category |
| GET | `/api/v1/categories` | Public | List all categories |

```json
{ "name": "Backend Engineering", "description": "APIs, databases, distributed systems" }
```

---
## 4. Posts — `/api/v1/posts`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/posts` | User | Create post (draft or published) |
| PUT | `/api/v1/posts/{id}` | Owner/Admin | Update own post |
| DELETE | `/api/v1/posts/{id}` | Owner/Admin | Delete own post |
| GET | `/api/v1/posts/{id}` | Public | View single post (increments view count) |
| GET | `/api/v1/posts` | Public | List/search/filter/paginate published posts |
| GET | `/api/v1/posts/me` | User | List my own posts (any status) |

**Query params for `GET /api/v1/posts`:**
`title`, `categoryId`, `page` (0-based), `size`, `sortBy` (`createdAt`, `title`, `viewCount`), `direction` (`asc`/`desc`)

Example: `GET /api/v1/posts?title=spring&categoryId=2&page=0&size=10&sortBy=createdAt&direction=desc`

**POST /api/v1/posts**
```json
{
  "title": "Getting Started with Spring Security 6",
  "content": "Full article body in markdown or HTML...",
  "summary": "A practical intro to Spring Security 6 with JWT.",
  "coverImageUrl": "https://cdn.example.com/cover.png",
  "categoryId": 2,
  "status": "PUBLISHED"
}
```

Response `201`:
```json
{
  "success": true,
  "message": "Post created",
  "data": {
    "id": 10,
    "title": "Getting Started with Spring Security 6",
    "slug": "getting-started-with-spring-security-6",
    "status": "PUBLISHED",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "likedByCurrentUser": false,
    "author": { "id": 1, "username": "johndoe", "fullName": "John Doe" },
    "category": { "id": 2, "name": "Backend Engineering", "postCount": 0 },
    "createdAt": "2026-07-15T10:05:00"
  }
}
```

---
## 5. Comments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/posts/{postId}/comments` | User | Add comment to a post |
| GET | `/api/v1/posts/{postId}/comments` | Public | Paginated comments for a post |
| PUT | `/api/v1/comments/{commentId}` | Owner/Admin | Edit own comment |
| DELETE | `/api/v1/comments/{commentId}` | Owner/Admin | Delete own comment |

```json
{ "content": "Great write-up, thanks for sharing!" }
```

---
## 6. Likes

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/posts/{postId}/like` | User | Toggle like/unlike (idempotent per user, duplicate-safe) |

Response:
```json
{ "success": true, "message": "Post liked", "data": { "postId": 10, "liked": true, "totalLikes": 5 } }
```

---
## 7. Admin — `/api/v1/admin` (ROLE_ADMIN only)

| Method | Endpoint | Description |
|---|---|---|
| DELETE | `/api/v1/admin/posts/{postId}` | Delete any post |
| DELETE | `/api/v1/admin/comments/{commentId}` | Delete any comment |
| GET | `/api/v1/admin/users?page=&size=` | Paginated list of all users |
| PATCH | `/api/v1/admin/users/{userId}/status?enabled=true` | Enable/disable a user |
| PUT | `/api/v1/admin/users/{userId}/roles` | Change a user's roles |
| DELETE | `/api/v1/admin/users/{userId}` | Delete a user |
| GET | `/api/v1/admin/dashboard` | Aggregate platform statistics |

```json
{ "roles": ["ROLE_USER", "ROLE_ADMIN"] }
```

Dashboard response:
```json
{
  "success": true,
  "message": "Dashboard stats fetched",
  "data": {
    "totalUsers": 128,
    "totalPosts": 542,
    "publishedPosts": 480,
    "draftPosts": 62,
    "totalComments": 1893,
    "totalLikes": 6210,
    "totalCategories": 12
  }
}
```

---
## Standard error shape (all 4xx/5xx)

```json
{
  "timestamp": "2026-07-15T10:12:00",
  "status": 404,
  "error": "Not Found",
  "message": "Post not found with id: 999",
  "path": "/api/v1/posts/999"
}
```

Validation errors additionally include a `validationErrors` map of `field -> message`.
