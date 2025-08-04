package com.training.warehouse.service;

import com.training.warehouse.dto.request.UpdateMyInfoRequest;
import com.training.warehouse.dto.response.GetMyInforResponse;
import com.training.warehouse.dto.response.UpdateMyInfoResponse;
import com.training.warehouse.entity.UserEntity;

public interface UserService {
    UpdateMyInfoResponse updateMyInfo(UserEntity user, UpdateMyInfoRequest request);
    GetMyInforResponse getMyInfo(UserEntity user);
}
