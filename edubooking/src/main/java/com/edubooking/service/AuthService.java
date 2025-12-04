package com.edubooking.service;

import com.edubooking.dto.AuthResponse;
import com.edubooking.dto.LoginRequest;
import com.edubooking.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
