package com.training.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileUploadResult {
    private String fileName;
    private boolean uploaded;
    private boolean savedToDB;
    private String errorMessage;
}
