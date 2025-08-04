package com.training.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMyInfoRequest {

    @NotBlank(message = "full name is required")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "email is required")
    @Email(message = "email is not valid")
    @JsonProperty("email")
    private String email;
}
