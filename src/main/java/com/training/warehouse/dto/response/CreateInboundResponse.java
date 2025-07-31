package com.training.warehouse.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateInboundResponse {
    @JsonProperty("id")
    private long id;
}