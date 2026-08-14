package com.schoolerp.usermanagement.common.util;

import com.schoolerp.usermanagement.common.exception.ValidationException;

public final class ValidationUtil {
    private ValidationUtil() {}

    public static void requireNonNull(Object obj, String message) {
        if (obj == null) throw new ValidationException(message);
    }

    public static void requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new ValidationException(message);
    }
}
