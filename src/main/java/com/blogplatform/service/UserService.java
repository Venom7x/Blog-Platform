package com.blogplatform.service;

import com.blogplatform.dto.request.ChangePasswordRequest;
import com.blogplatform.dto.request.UpdateProfileRequest;
import com.blogplatform.dto.response.UserResponse;

public interface UserService {
    UserResponse getOwnProfile(Long userId);
    UserResponse updateOwnProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    void deleteOwnAccount(Long userId);
    UserResponse getPublicProfile(String username);
}
