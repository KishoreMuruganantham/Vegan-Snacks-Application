package com.examly.springapp.exception;

/**
 * Raised when a catalogue entry is addressed by an identifier that does not exist.
 */
public class SnackNotFoundException extends RuntimeException {

    /** Serialisation identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception for the given identifier.
     *
     * @param id identifier that could not be resolved
     */
    public SnackNotFoundException(Long id) {
        super("Vegan snack not found with id " + id);
    }
}
