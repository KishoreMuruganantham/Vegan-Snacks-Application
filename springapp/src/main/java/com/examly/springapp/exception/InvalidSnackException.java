package com.examly.springapp.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Raised when a submitted payload violates one or more catalogue rules.
 *
 * <p>The exception carries the complete list of detected problems so that the client receives
 * every faulty field in a single response.</p>
 */
public class InvalidSnackException extends RuntimeException {

    /** Summary reported when the payload is rejected. */
    public static final String DEFAULT_MESSAGE = "Snack details are invalid.";

    /** Serialisation identifier. */
    private static final long serialVersionUID = 1L;

    /** Map of field name to validation message. */
    private final Map<String, String> fieldErrors;

    /**
     * Creates an exception carrying the detected field errors.
     *
     * @param fieldErrors map of field name to validation message
     */
    public InvalidSnackException(Map<String, String> fieldErrors) {
        this(DEFAULT_MESSAGE, fieldErrors);
    }

    /**
     * Creates an exception carrying the detected field errors and a custom summary.
     *
     * @param message     summary reported to the client
     * @param fieldErrors map of field name to validation message
     */
    public InvalidSnackException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldErrors);
    }

    /**
     * Returns the detected field errors.
     *
     * @return map of field name to validation message
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
