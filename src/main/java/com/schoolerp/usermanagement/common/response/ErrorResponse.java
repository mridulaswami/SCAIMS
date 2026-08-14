package com.schoolerp.usermanagement.common.response;

import java.time.Instant;

public class ErrorResponse {
    private Instant timestamp = Instant.now();
    private int status;
    private Object error;
    private String message;
    private String path;

    public ErrorResponse() {}

    public ErrorResponse(int status, Object error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
