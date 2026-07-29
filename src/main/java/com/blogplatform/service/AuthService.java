package com.blogplatform.service;

import com.blogplatform.dto.request.LoginRequest;
import com.blogplatform.dto.request.RegisterRequest;
import com.blogplatform.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
