package com.examly.springapp.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Raised when a snack is submitted with a name that is already used by another catalogue entry.
 *
 * <p>Names are compared without regard to letter case so that the catalogue cannot contain two
 * entries that only differ in capitalisation.</p>
 */
public class DuplicateSnackException extends RuntimeException {

    /** Summary reported when the duplicate is rejected. */
    public static final String DEFAULT_MESSAGE = "A snack with this name already exists";

    /** Serialisation identifier. */
    private static final long serialVersionUID = 1L;

    /** Map of field name to validation message. */
    private final Map<String, String> fieldErrors;

    /** Name that is already used by another catalogue entry. */
    private final String snackName;

    /**
     * Creates an exception for the given snack name.
     *
     * @param snackName name that is already used by another catalogue entry
     */
    public DuplicateSnackException(String snackName) {
        super(DEFAULT_MESSAGE);
        this.snackName = snackName;
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("snackName", DEFAULT_MESSAGE);
        this.fieldErrors = errors;
    }

    /**
     * Returns the name that was rejected.
     *
     * @return the duplicated snack name
     */
    public String getSnackName() {
        return snackName;
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
