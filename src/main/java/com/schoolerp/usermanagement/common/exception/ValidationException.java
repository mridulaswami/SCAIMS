package com.schoolerp.usermanagement.common.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(400, message);
    }
}
