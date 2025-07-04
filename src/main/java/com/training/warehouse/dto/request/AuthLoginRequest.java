package com.training.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginRequest {

    @NotBlank(message = "username is required")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "password is required")
    @JsonProperty("password")
    private String password;
}
