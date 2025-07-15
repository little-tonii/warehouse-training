package com.training.warehouse.service.impl;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.training.warehouse.dto.request.UserUpdateInfoRequest;
import com.training.warehouse.dto.response.UserUpdateInfoResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.exception.ConflicException;
import com.training.warehouse.exception.UnauthorizedException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.UserRepository;
import com.training.warehouse.service.UserService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; 

    @Override
    public UserUpdateInfoResponse updateInfo(UserUpdateInfoRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserEntity> userResult = this.userRepository.findByUsername(username);
        if (!userResult.isPresent()) {
            throw new UnauthorizedException(ExceptionMessage.UNAUTHORIZED);
        }
        UserEntity user = userResult.get();
        Optional<UserEntity> userByEmail = this.userRepository.findByEmail(request.getEmail());
        if (userByEmail.isPresent() && userByEmail.get().getId() != user.getId()) {
            throw new ConflicException(ExceptionMessage.EMAIL_ALREADY_EXIST);
        }
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        UserEntity updatedUser = this.userRepository.save(user);
        return UserUpdateInfoResponse.builder()
                .id(updatedUser.getId())
                .build();
    }
}
