package com.training.warehouse.common.util;

import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Optional;

import static com.training.warehouse.service.FileStoreService.ALLOWED_CONTENT_TYPES;

public class FileUtil {
    private FileUtil(){
    }

    public static void validateFileName(String fileName){
        String name = Optional.ofNullable(fileName)
                .map(n -> Paths.get(n).getFileName().toString())
                .orElseThrow(() -> new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID));

        if (name.isBlank() || !name.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
    }
}
