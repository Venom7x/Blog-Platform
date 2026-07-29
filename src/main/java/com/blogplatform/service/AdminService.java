package com.blogplatform.service;

import com.blogplatform.dto.response.DashboardStatsResponse;
import com.blogplatform.dto.response.PageResponse;
import com.blogplatform.dto.response.UserResponse;
import com.blogplatform.dto.request.UpdateUserRolesRequest;

public interface AdminService {
    void deleteAnyPost(Long postId);
    void deleteAnyComment(Long commentId);
    PageResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse setUserEnabled(Long userId, boolean enabled);
    UserResponse updateUserRoles(Long userId, UpdateUserRolesRequest request);
    void deleteUser(Long userId);
    DashboardStatsResponse getDashboardStats();
}
