package com.training.warehouse.service.impl;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.training.warehouse.common.provider.JwtProvider;
import com.training.warehouse.dto.request.AuthLoginRequest;
import com.training.warehouse.dto.request.AuthRegisterRequest;
import com.training.warehouse.dto.response.AuthLoginResponse;
import com.training.warehouse.dto.response.AuthRegisterResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.Role;
import com.training.warehouse.exception.ConflicException;
import com.training.warehouse.exception.UnauthorizedException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.UserRepository;
import com.training.warehouse.service.AuthService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthRegisterResponse register(AuthRegisterRequest request) {
        Optional<UserEntity> user = this.userRepository.findByUsername(request.getUsername());
        if (user.isPresent()) {
            throw new ConflicException(ExceptionMessage.USER_ALREADY_EXIST);
        }
        user = this.userRepository.findByEmail(request.getEmail());
        if (user.isPresent()) {
            throw new ConflicException(ExceptionMessage.USER_ALREADY_EXIST);
        }
        UserEntity newUser = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .build();
        newUser = this.userRepository.save(newUser);
        return AuthRegisterResponse.builder()
                .id(newUser.getId())
                .build();
    }

    @Override
    public AuthLoginResponse login(AuthLoginRequest request) {
        Optional<UserEntity> user = this.userRepository.findByUsername(request.getUsername());
        if (!user.isPresent() || !this.passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            throw new UnauthorizedException(ExceptionMessage.UNAUTHORIZED);
        }
        UserEntity presentUser = user.get();
        Map<String, Object> claims = Map.of(
                "id", presentUser.getId(),
                "username", presentUser.getUsername(),
                "email", presentUser.getEmail(),
                "token_version", presentUser.getTokenVersion()
        );
        String token = this.jwtProvider.generateToken(claims, presentUser);
        return AuthLoginResponse.builder()
                .accessToken(token)
                .build();
    }
}
