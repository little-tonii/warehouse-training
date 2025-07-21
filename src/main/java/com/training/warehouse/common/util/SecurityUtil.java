package com.training.warehouse.common.util;

import com.training.warehouse.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static UserEntity getCurrentUser() {
        return (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}