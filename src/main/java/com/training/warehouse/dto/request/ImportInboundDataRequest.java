package com.training.warehouse.dto.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportInboundDataRequest {
    @Min(value = 1, message = "only accept 1 data file")
    @Max(value = 1, message = "only accept 1 data file")
    @NotNull(message = "data file must not be null")
    private List<MultipartFile> dataFiles;
}
