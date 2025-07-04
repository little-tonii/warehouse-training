package com.training.warehouse.service;

import com.training.warehouse.dto.request.AuthLoginRequest;
import com.training.warehouse.dto.request.AuthRegisterRequest;
import com.training.warehouse.dto.response.AuthLoginResponse;
import com.training.warehouse.dto.response.AuthRegisterResponse;

public interface AuthService {
    AuthRegisterResponse register(AuthRegisterRequest request);
    AuthLoginResponse login(AuthLoginRequest request);
}
