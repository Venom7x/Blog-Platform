package com.blogplatform.controller;

import com.blogplatform.dto.request.ChangePasswordRequest;
import com.blogplatform.dto.request.UpdateProfileRequest;
import com.blogplatform.dto.response.ApiResponse;
import com.blogplatform.dto.response.UserResponse;
import com.blogplatform.security.CustomUserDetails;
import com.blogplatform.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Own profile management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getOwnProfile(@AuthenticationPrincipal CustomUserDetails principal) {
        UserResponse response = userService.getOwnProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateOwnProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateOwnProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteOwnAccount(@AuthenticationPrincipal CustomUserDetails principal) {
        userService.deleteOwnAccount(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getPublicProfile(@PathVariable String username) {
        UserResponse response = userService.getPublicProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }
}
