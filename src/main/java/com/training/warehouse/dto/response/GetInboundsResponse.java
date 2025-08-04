package com.training.warehouse.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetInboundsResponse {

    @JsonProperty("inbounds")
    private List<GetInboundByIdResponse> inbounds;

    @JsonProperty("total")
    private long total;

    @JsonProperty("page")
    private long page;

    @JsonProperty("limit")
    private long limit;
}
