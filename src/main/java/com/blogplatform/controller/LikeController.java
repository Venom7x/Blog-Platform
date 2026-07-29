package com.blogplatform.controller;

import com.blogplatform.dto.response.ApiResponse;
import com.blogplatform.dto.response.LikeResponse;
import com.blogplatform.security.CustomUserDetails;
import com.blogplatform.service.LikeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Likes", description = "Like / unlike a post (toggle, duplicate-safe)")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/api/v1/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long postId) {
        LikeResponse response = likeService.toggleLike(postId, principal.getId());
        String message = response.isLiked() ? "Post liked" : "Post unliked";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}
