-- ============================================================
-- Blogging Platform - Reference Relational Schema (MySQL 8)
-- Note: Hibernate (ddl-auto=update) will create/evolve this schema
-- automatically at startup. This file is for reference / manual
-- provisioning / documentation purposes.
-- ============================================================

CREATE DATABASE IF NOT EXISTS blog_platform;
USE blog_platform;

-- ---------------- Roles ----------------
CREATE TABLE roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE   -- ROLE_USER, ROLE_ADMIN
);

-- ---------------- Users ----------------
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150),
    bio           VARCHAR(500),
    avatar_url    VARCHAR(500),
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME,
    created_by    VARCHAR(100),
    modified_by   VARCHAR(100),
    version       BIGINT
);

-- ---------------- User <-> Role (Many-to-Many) ----------------
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ---------------- Categories ----------------
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300),
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME,
    created_by  VARCHAR(100),
    modified_by VARCHAR(100),
    version     BIGINT
);

-- ---------------- Posts ----------------
CREATE TABLE posts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(250) NOT NULL,
    slug            VARCHAR(280) NOT NULL UNIQUE,
    content         LONGTEXT NOT NULL,
    summary         VARCHAR(500),
    cover_image_url VARCHAR(500),
    status          VARCHAR(20) NOT NULL,     -- DRAFT | PUBLISHED
    view_count      BIGINT NOT NULL DEFAULT 0,
    author_id       BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME,
    created_by      VARCHAR(100),
    modified_by     VARCHAR(100),
    version         BIGINT,
    FOREIGN KEY (author_id)   REFERENCES users(id)      ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    INDEX idx_post_title  (title),
    INDEX idx_post_status (status)
);

-- ---------------- Comments ----------------
CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT NOT NULL,
    post_id     BIGINT NOT NULL,
    author_id   BIGINT NOT NULL,
    created_at  DATETIME NOT NULL,
    updated_at  DATETIME,
    created_by  VARCHAR(100),
    modified_by VARCHAR(100),
    version     BIGINT,
    FOREIGN KEY (post_id)   REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id)  ON DELETE CASCADE,
    INDEX idx_comment_post (post_id)
);

-- ---------------- Post Likes ----------------
CREATE TABLE post_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_post_user_like UNIQUE (post_id, user_id)  -- prevents duplicate likes
);
