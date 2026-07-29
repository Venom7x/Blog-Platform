package com.blogplatform.controller;

import com.blogplatform.dto.request.PostRequest;
import com.blogplatform.dto.response.ApiResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.PostResponse;
import com.blogplatform.security.CustomUserDetails;
import com.blogplatform.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Create, read, update, delete, search & filter blog posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PostRequest request) {
        PostResponse response = postService.createPost(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Post created", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        PostResponse response = postService.updatePost(id, principal.getId(), isAdmin, request);
        return ResponseEntity.ok(ApiResponse.success("Post updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        postService.deletePost(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Post deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        Long currentUserId = principal != null ? principal.getId() : null;
        PostResponse response = postService.getPostById(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Post fetched", response));
    }

    /**
     * Public feed: search by title, filter by category, paginate & sort.
     * Example: GET /api/v1/posts?title=spring&categoryId=2&page=0&size=10&sortBy=createdAt&direction=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getAllPosts(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Long currentUserId = principal != null ? principal.getId() : null;
        PageResponse<PostResponse> response = postService.getAllPublishedPosts(
                title, categoryId, page, size, sortBy, direction, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Posts fetched", response));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<PostResponse>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        PageResponse<PostResponse> response = postService.getMyPosts(principal.getId(), page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success("Your posts fetched", response));
    }
}
