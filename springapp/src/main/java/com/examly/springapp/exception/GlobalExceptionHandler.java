package com.examly.springapp.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.examly.springapp.dto.response.ApiErrorResponse;

/**
 * Translates the exceptions raised by the service layer into the uniform error payload.
 *
 * <p>Every response produced here uses the {@link ApiErrorResponse} structure so that a client
 * can rely on a single error format. Internal details such as stack traces are never exposed;
 * unexpected failures are logged on the server and reported with a neutral message.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Logger used to record unexpected failures. */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles payloads that violate the catalogue rules.
     *
     * @param exception raised by the service layer
     * @return a bad request response carrying every detected field error
     */
    @ExceptionHandler(InvalidSnackException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSnack(InvalidSnackException exception) {
        logger.debug("Rejected snack payload: {}", exception.getFieldErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        exception.getMessage(), exception.getFieldErrors()));
    }

    /**
     * Handles submissions that reuse a name already present in the catalogue.
     *
     * @param exception raised by the service layer
     * @return a conflict response carrying the offending field
     */
    @ExceptionHandler(DuplicateSnackException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSnack(DuplicateSnackException exception) {
        logger.debug("Rejected duplicate snack: {}", exception.getSnackName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Conflict",
                        exception.getMessage(), exception.getFieldErrors()));
    }

    /**
     * Handles addresses that reference a catalogue entry which does not exist.
     *
     * @param exception raised by the service layer
     * @return a not found response
     */
    @ExceptionHandler(SnackNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSnackNotFound(SnackNotFoundException exception) {
        logger.debug("Catalogue entry not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found",
                        exception.getMessage(), Map.of()));
    }

    /**
     * Handles requests whose body could not be bound to the expected structure.
     *
     * @param exception raised by the message converter
     * @return a bad request response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        "Snack details are invalid.", fieldErrors));
    }

    /**
     * Handles request bodies that are missing or cannot be parsed.
     *
     * @param exception raised by the message converter
     * @return a bad request response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        logger.debug("Rejected unreadable request body: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                        "Request body is missing or malformed.", Map.of()));
    }

    /**
     * Handles every remaining failure.
     *
     * <p>Errors raised by the framework itself, for example an unsupported HTTP method, keep their
     * original status code. Everything else is reported as an internal server error and logged
     * with the full stack trace on the server only.</p>
     *
     * @param exception raised while processing the request
     * @return the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.resolve(errorResponse.getStatusCode().value());
            HttpStatus resolvedStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
            String detail = errorResponse.getBody() == null ? null : errorResponse.getBody().getDetail();
            String message = detail == null || detail.isBlank() ? resolvedStatus.getReasonPhrase() : detail;
            logger.warn("Request rejected with status {}", resolvedStatus.value());
            return ResponseEntity.status(resolvedStatus)
                    .body(ApiErrorResponse.of(resolvedStatus.value(), resolvedStatus.getReasonPhrase(),
                            message, Map.of()));
        }
        logger.error("Unexpected failure while processing request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                        "An unexpected error occurred while processing the request.", Map.of()));
    }
}
