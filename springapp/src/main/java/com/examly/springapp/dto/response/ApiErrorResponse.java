package com.examly.springapp.dto.response;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Uniform error payload returned for every unsuccessful request.
 *
 * <p>The payload never exposes internal implementation details. Field level problems are reported
 * in {@link #getFieldErrors()} so that a client can map them back onto the form controls.</p>
 */
public class ApiErrorResponse {

    /** Point in time at which the error was produced. */
    private LocalDateTime timestamp;

    /** HTTP status code of the response. */
    private int status;

    /** Reason phrase matching the status code. */
    private String error;

    /** Human readable summary of the failure. */
    private String message;

    /** Map of field name to validation message, empty when no field could be identified. */
    private Map<String, String> fieldErrors = new LinkedHashMap<>();

    /**
     * Creates an empty payload.
     */
    public ApiErrorResponse() {
        // Required for JSON serialisation.
    }

    /**
     * Creates a fully initialised payload.
     *
     * @param timestamp   point in time at which the error was produced
     * @param status      HTTP status code
     * @param error       reason phrase matching the status code
     * @param message     human readable summary of the failure
     * @param fieldErrors map of field name to validation message
     */
    public ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                            Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldErrors);
    }

    /**
     * Builds an error payload stamped with the current time.
     *
     * @param status      HTTP status code
     * @param error       reason phrase matching the status code
     * @param message     human readable summary of the failure
     * @param fieldErrors map of field name to validation message
     * @return the assembled payload
     */
    public static ApiErrorResponse of(int status, String error, String message,
                                      Map<String, String> fieldErrors) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, message, fieldErrors);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldErrors);
    }
}
