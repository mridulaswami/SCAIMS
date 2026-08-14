package com.schoolerp.usermanagement.common.exception;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
