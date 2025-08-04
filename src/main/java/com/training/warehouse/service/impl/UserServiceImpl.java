package com.training.warehouse.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.training.warehouse.dto.request.UpdateMyInfoRequest;
import com.training.warehouse.dto.response.GetMyInforResponse;
import com.training.warehouse.dto.response.UpdateMyInfoResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.exception.ConflicException;
import com.training.warehouse.exception.UnauthorizedException;
import com.training.warehouse.repository.UserRepository;
import com.training.warehouse.service.UserService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; 

    @Override
    public UpdateMyInfoResponse updateMyInfo(UserEntity user, UpdateMyInfoRequest request) {
        Optional<UserEntity> userResult = this.userRepository.findByUsername(user.getUsername());
        if (!userResult.isPresent()) {
            throw new UnauthorizedException("unauthorized");
        }
        Optional<UserEntity> userByEmail = this.userRepository.findByEmail(request.getEmail());
        if (userByEmail.isPresent() && userByEmail.get().getId() != user.getId()) {
            throw new ConflicException("email already exists");
        }
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        UserEntity updatedUser = this.userRepository.save(user);
        return UpdateMyInfoResponse.builder()
                .id(updatedUser.getId())
                .build();
    }

    @Override
    public GetMyInforResponse getMyInfo(UserEntity user) {
        return GetMyInforResponse.builder()
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}
