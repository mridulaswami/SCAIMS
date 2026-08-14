package com.schoolerp.usermanagement.common.exception;

public class DuplicateResourceException extends ApiException {
    public DuplicateResourceException(String message) {
        super(409, message);
    }
}
