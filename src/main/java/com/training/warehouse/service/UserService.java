package com.training.warehouse.service;

import com.training.warehouse.dto.request.UserUpdateInfoRequest;
import com.training.warehouse.dto.response.UserUpdateInfoResponse;

public interface UserService {
    UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request);
}
