package com.training.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegisterRequest {

    @JsonProperty("username")
    @NotBlank(message = "username is required")
    private String username;

    @JsonProperty("password")
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @JsonProperty("email")
    @NotBlank(message = "email is required")
    @Email(message = "email is not valid")
    private String email;
}
