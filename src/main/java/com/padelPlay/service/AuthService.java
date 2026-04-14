package com.padel.padel_backend.service;

import com.padel.padel_backend.dto.request.LoginRequest;
import com.padel.padel_backend.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}