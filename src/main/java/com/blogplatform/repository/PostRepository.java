package com.blogplatform.repository;

import com.blogplatform.entity.Post;
import com.blogplatform.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Page<Post> findAllByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("""
           SELECT p FROM Post p
           WHERE p.status = :status
             AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
             AND (:categoryId IS NULL OR p.category.id = :categoryId)
           """)
    Page<Post> searchPosts(@Param("status") PostStatus status,
                            @Param("title") String title,
                            @Param("categoryId") Long categoryId,
                            Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Post> findWithDetailsById(Long id);

    long countByStatus(PostStatus status);

    long countByAuthorId(Long authorId);
}
