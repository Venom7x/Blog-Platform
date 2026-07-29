package com.blogplatform.controller;

import com.blogplatform.dto.request.UpdateUserRolesRequest;
import com.blogplatform.dto.response.ApiResponse;
import com.blogplatform.dto.response.DashboardStatsResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.UserResponse;
import com.blogplatform.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only moderation & dashboard endpoints")
public class AdminController {

    private final AdminService adminService;

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnyPost(@PathVariable Long postId) {
        adminService.deleteAnyPost(postId);
        return ResponseEntity.ok(ApiResponse.success("Post deleted by admin"));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnyComment(@PathVariable Long commentId) {
        adminService.deleteAnyComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted by admin"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", adminService.getAllUsers(page, size)));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> setUserEnabled(
            @PathVariable Long userId, @RequestParam boolean enabled) {
        UserResponse response = adminService.setUserEnabled(userId, enabled);
        String msg = enabled ? "User enabled" : "User disabled";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long userId, @Valid @RequestBody UpdateUserRolesRequest request) {
        UserResponse response = adminService.updateUserRoles(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User roles updated", response));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", adminService.getDashboardStats()));
    }
}
