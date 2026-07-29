# Entity-Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ POSTS : "authors"
    USERS ||--o{ COMMENTS : "writes"
    USERS ||--o{ POST_LIKES : "likes"
    USERS }o--o{ ROLES : "has (user_roles)"
    CATEGORIES ||--o{ POSTS : "classifies"
    POSTS ||--o{ COMMENTS : "has"
    POSTS ||--o{ POST_LIKES : "receives"

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password_hash
        varchar full_name
        varchar bio
        varchar avatar_url
        boolean enabled
        datetime created_at
        datetime updated_at
    }

    ROLES {
        bigint id PK
        varchar name UK
    }

    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
    }

    CATEGORIES {
        bigint id PK
        varchar name UK
        varchar description
    }

    POSTS {
        bigint id PK
        varchar title
        varchar slug UK
        longtext content
        varchar summary
        varchar status
        bigint view_count
        bigint author_id FK
        bigint category_id FK
        datetime created_at
        datetime updated_at
    }

    COMMENTS {
        bigint id PK
        text content
        bigint post_id FK
        bigint author_id FK
        datetime created_at
        datetime updated_at
    }

    POST_LIKES {
        bigint id PK
        bigint post_id FK
        bigint user_id FK
        datetime created_at
    }
```

## Relationship summary

| Relationship | Type | Notes |
|---|---|---|
| User → Post | One-to-Many | `Post.author_id` FK; `cascade=ALL, orphanRemoval` from User |
| Category → Post | One-to-Many | `Post.category_id` FK; deleting a category is `RESTRICT`ed while posts reference it |
| Post → Comment | One-to-Many | `Comment.post_id` FK; cascades on post delete |
| User → Comment | One-to-Many | `Comment.author_id` FK |
| Post ↔ User (via PostLike) | Many-to-Many (join entity) | Modeled explicitly as `PostLike` (not `@ManyToMany`) so we can store `created_at` and enforce a unique `(post_id, user_id)` constraint against duplicate likes |
| User ↔ Role | Many-to-Many | Standard `@ManyToMany` with `user_roles` join table |
