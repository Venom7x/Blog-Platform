-- ============================================================================
--  Blogging Platform — Complete MySQL Database
--  Engine target: MySQL 8.0+
--  Matches the JPA/Hibernate entity model exactly (column names, types,
--  nullability, unique constraints, FKs) so it is safe to run this script
--  directly OR let Hibernate's ddl-auto=update manage it — both stay in sync.
--
--  Run with:  mysql -u root -p < database.sql
-- ============================================================================

DROP DATABASE IF EXISTS blog_platform;
CREATE DATABASE blog_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE blog_platform;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. ROLES
-- ============================================================================
CREATE TABLE roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    CONSTRAINT uq_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 2. USERS
-- ============================================================================
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(150) NULL,
    bio           VARCHAR(500) NULL,
    avatar_url    VARCHAR(500) NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,

    -- auditing (BaseEntity)
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NULL,
    created_by    VARCHAR(100) NULL,
    modified_by   VARCHAR(100) NULL,
    version       BIGINT       NULL DEFAULT 0,

    CONSTRAINT uq_user_username UNIQUE (username),
    CONSTRAINT uq_user_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_user_email    ON users (email);
CREATE INDEX idx_user_username ON users (username);

-- ============================================================================
-- 3. USER_ROLES  (User <-> Role, Many-to-Many join table)
-- ============================================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 4. CATEGORIES
-- ============================================================================
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(300) NULL,

    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    created_by  VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    version     BIGINT       NULL DEFAULT 0,

    CONSTRAINT uq_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 5. POSTS
-- ============================================================================
CREATE TABLE posts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(250)  NOT NULL,
    slug            VARCHAR(280)  NOT NULL,
    content         LONGTEXT      NOT NULL,
    summary         VARCHAR(500)  NULL,
    cover_image_url VARCHAR(500)  NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',   -- DRAFT | PUBLISHED
    view_count      BIGINT        NOT NULL DEFAULT 0,
    author_id       BIGINT        NOT NULL,
    category_id     BIGINT        NOT NULL,

    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NULL,
    created_by      VARCHAR(100)  NULL,
    modified_by     VARCHAR(100)  NULL,
    version         BIGINT        NULL DEFAULT 0,

    CONSTRAINT uq_post_slug UNIQUE (slug),
    CONSTRAINT chk_post_status CHECK (status IN ('DRAFT','PUBLISHED')),
    CONSTRAINT fk_post_author   FOREIGN KEY (author_id)   REFERENCES users(id)      ON DELETE CASCADE,
    CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_post_title  ON posts (title);
CREATE INDEX idx_post_status ON posts (status);
CREATE INDEX idx_post_author ON posts (author_id);
CREATE INDEX idx_post_category ON posts (category_id);
CREATE FULLTEXT INDEX ftx_post_title_content ON posts (title, summary); -- optional: enables MATCH/AGAINST full-text search

-- ============================================================================
-- 6. COMMENTS
-- ============================================================================
CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    content     TEXT          NOT NULL,
    post_id     BIGINT        NOT NULL,
    author_id   BIGINT        NOT NULL,

    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NULL,
    created_by  VARCHAR(100)  NULL,
    modified_by VARCHAR(100)  NULL,
    version     BIGINT        NULL DEFAULT 0,

    CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comment_post   ON comments (post_id);
CREATE INDEX idx_comment_author ON comments (author_id);

-- ============================================================================
-- 7. POST_LIKES  (Post <-> User join entity; unique constraint blocks duplicates)
-- ============================================================================
CREATE TABLE post_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT uq_post_user_like UNIQUE (post_id, user_id),
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_like_post ON post_likes (post_id);
CREATE INDEX idx_like_user ON post_likes (user_id);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- SEED DATA
-- ============================================================================

-- ---------------- Roles ----------------
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');

-- ---------------- Users ----------------
-- Passwords below are real BCrypt hashes (strength 12), compatible with
-- Spring Security's BCryptPasswordEncoder out of the box.
--   admin   / Admin@12345
--   johndoe / User@12345
--   janedoe / User@12345
INSERT INTO users (username, email, password_hash, full_name, bio, enabled, created_at, created_by, version) VALUES
('admin',   'admin@blogplatform.com',  '$2b$12$mBot.fkbADfQzqYHwTm3vu8XegL5bg0jfn5gFv0tjpmLTpnBje7zS', 'Platform Admin', 'I keep the lights on.',            TRUE, NOW(6), 'SYSTEM', 0),
('johndoe', 'john@example.com',        '$2b$12$W9bvfHQfaq3p0g1AvlvSaek4xp1BtEss3khkZx/LoPe2pcq8zKRhK', 'John Doe',       'Backend engineer. Writes about Java & Spring.', TRUE, NOW(6), 'SYSTEM', 0),
('janedoe', 'jane@example.com',        '$2b$12$W9bvfHQfaq3p0g1AvlvSaek4xp1BtEss3khkZx/LoPe2pcq8zKRhK', 'Jane Doe',       'Frontend dev exploring backend systems.',       TRUE, NOW(6), 'SYSTEM', 0);

-- ---------------- User <-> Role assignments ----------------
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin'   AND r.name = 'ROLE_ADMIN';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin'   AND r.name = 'ROLE_USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'johndoe' AND r.name = 'ROLE_USER';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'janedoe' AND r.name = 'ROLE_USER';

-- ---------------- Categories ----------------
INSERT INTO categories (name, description, created_at, created_by, version) VALUES
('Backend Engineering', 'APIs, databases, distributed systems',        NOW(6), 'SYSTEM', 0),
('Frontend Development', 'UI frameworks, styling, browser tech',        NOW(6), 'SYSTEM', 0),
('DevOps',               'CI/CD, containers, infrastructure',           NOW(6), 'SYSTEM', 0),
('Career',                'Interviews, growth, soft skills',             NOW(6), 'SYSTEM', 0);

-- ---------------- Posts ----------------
INSERT INTO posts (title, slug, content, summary, status, view_count, author_id, category_id, created_at, created_by, version) VALUES
('Getting Started with Spring Security 6',
 'getting-started-with-spring-security-6',
 'Full article body explaining Spring Security 6 filter chains, JWT integration, and method security...',
 'A practical introduction to Spring Security 6 with JWT.',
 'PUBLISHED', 120,
 (SELECT id FROM users WHERE username = 'johndoe'),
 (SELECT id FROM categories WHERE name = 'Backend Engineering'),
 NOW(6), 'johndoe', 0),

('Designing a Clean REST API',
 'designing-a-clean-rest-api',
 'This article walks through REST conventions, DTO boundaries, pagination, and error handling...',
 'Principles for building maintainable, predictable REST APIs.',
 'PUBLISHED', 85,
 (SELECT id FROM users WHERE username = 'johndoe'),
 (SELECT id FROM categories WHERE name = 'Backend Engineering'),
 NOW(6), 'johndoe', 0),

('My Draft on React Server Components',
 'my-draft-on-react-server-components',
 'Work in progress notes on RSC architecture...',
 'Early notes, not ready yet.',
 'DRAFT', 0,
 (SELECT id FROM users WHERE username = 'janedoe'),
 (SELECT id FROM categories WHERE name = 'Frontend Development'),
 NOW(6), 'janedoe', 0),

('Dockerizing a Spring Boot Application',
 'dockerizing-a-spring-boot-application',
 'Step-by-step guide to building a multi-stage Dockerfile for a Spring Boot app...',
 'A practical guide to containerizing your Spring Boot services.',
 'PUBLISHED', 42,
 (SELECT id FROM users WHERE username = 'janedoe'),
 (SELECT id FROM categories WHERE name = 'DevOps'),
 NOW(6), 'janedoe', 0);

-- ---------------- Comments ----------------
INSERT INTO comments (content, post_id, author_id, created_at, created_by, version) VALUES
('Great write-up, thanks for sharing!',
 (SELECT id FROM posts WHERE slug = 'getting-started-with-spring-security-6'),
 (SELECT id FROM users WHERE username = 'janedoe'),
 NOW(6), 'janedoe', 0),

('Could you cover refresh token rotation in a follow-up?',
 (SELECT id FROM posts WHERE slug = 'getting-started-with-spring-security-6'),
 (SELECT id FROM users WHERE username = 'admin'),
 NOW(6), 'admin', 0),

('Solid guide, the multi-stage build tip saved me a lot of image size.',
 (SELECT id FROM posts WHERE slug = 'dockerizing-a-spring-boot-application'),
 (SELECT id FROM users WHERE username = 'johndoe'),
 NOW(6), 'johndoe', 0);

-- ---------------- Post Likes ----------------
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
((SELECT id FROM posts WHERE slug = 'getting-started-with-spring-security-6'), (SELECT id FROM users WHERE username = 'janedoe'), NOW(6)),
((SELECT id FROM posts WHERE slug = 'getting-started-with-spring-security-6'), (SELECT id FROM users WHERE username = 'admin'),   NOW(6)),
((SELECT id FROM posts WHERE slug = 'dockerizing-a-spring-boot-application'),  (SELECT id FROM users WHERE username = 'johndoe'), NOW(6));

-- ============================================================================
-- OPTIONAL: Helper view for admin dashboard-style aggregate queries
-- (The application computes these via repository count queries at runtime;
--  this view is provided for ad-hoc reporting / BI tools.)
-- ============================================================================
CREATE OR REPLACE VIEW v_platform_stats AS
SELECT
    (SELECT COUNT(*) FROM users)                                AS total_users,
    (SELECT COUNT(*) FROM posts)                                AS total_posts,
    (SELECT COUNT(*) FROM posts WHERE status = 'PUBLISHED')     AS published_posts,
    (SELECT COUNT(*) FROM posts WHERE status = 'DRAFT')         AS draft_posts,
    (SELECT COUNT(*) FROM comments)                             AS total_comments,
    (SELECT COUNT(*) FROM post_likes)                           AS total_likes,
    (SELECT COUNT(*) FROM categories)                           AS total_categories;

-- ============================================================================
-- Verification queries (optional — run manually to sanity-check the seed data)
-- ============================================================================
-- SELECT * FROM v_platform_stats;
-- SELECT u.username, r.name FROM users u JOIN user_roles ur ON u.id=ur.user_id JOIN roles r ON ur.role_id=r.id;
-- SELECT p.title, p.status, u.username AS author, c.name AS category FROM posts p
--   JOIN users u ON p.author_id=u.id JOIN categories c ON p.category_id=c.id;
