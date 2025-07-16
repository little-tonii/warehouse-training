package com.training.warehouse.common.util;

import com.training.warehouse.entity.CustomUserDetails;
import com.training.warehouse.entity.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        return (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static UserEntity getCurrentUser() {
        return getCurrentUserDetails().getUserEntity();
    }
}