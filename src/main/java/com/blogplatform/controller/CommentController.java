package com.blogplatform.controller;

import com.blogplatform.dto.request.CommentRequest;
import com.blogplatform.dto.response.ApiResponse;
import com.blogplatform.dto.response.CommentResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.security.CustomUserDetails;
import com.blogplatform.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment on posts, edit/delete own comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(postId, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Comment added", response));
    }

    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CommentResponse> response = commentService.getCommentsForPost(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Comments fetched", response));
    }

    @PutMapping("/api/v1/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        CommentResponse response = commentService.updateComment(commentId, principal.getId(), isAdmin, request);
        return ResponseEntity.ok(ApiResponse.success("Comment updated", response));
    }

    @DeleteMapping("/api/v1/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long commentId) {
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        commentService.deleteComment(commentId, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted"));
    }
}
