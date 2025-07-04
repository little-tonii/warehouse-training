package com.training.warehouse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.training.warehouse.dto.request.UserUpdateInfoRequest;
import com.training.warehouse.dto.response.UserUpdateInfoResponse;
import com.training.warehouse.service.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping
    public ResponseEntity<UserUpdateInfoResponse> updateInfo(UserUpdateInfoRequest request) {
        UserUpdateInfoResponse response = userService.updateInfo(request);
        return ResponseEntity.ok(response);
    }
}
