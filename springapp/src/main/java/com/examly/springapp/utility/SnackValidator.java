package com.examly.springapp.utility;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.examly.springapp.dto.request.VeganSnackRequest;

/**
 * Hand written validator for incoming snack payloads.
 *
 * <p>The application deliberately avoids annotation driven validation so that every rule and every
 * message stays in one place. The validator inspects a request and reports <em>all</em> problems it
 * finds instead of stopping at the first one, which allows the client to highlight every faulty
 * field in a single round trip.</p>
 */
@Component
public class SnackValidator {

    /** Minimum number of characters accepted for a snack name. */
    public static final int MIN_NAME_LENGTH = 2;

    /** Maximum number of characters accepted for a snack name. */
    public static final int MAX_NAME_LENGTH = 60;

    /** Maximum number of characters accepted for a snack type. */
    public static final int MAX_TYPE_LENGTH = 40;

    /** Minimum number of units that may be held in stock. */
    public static final int MIN_QUANTITY = 1;

    /** Maximum number of units that may be held in stock. */
    public static final int MAX_QUANTITY = 100000;

    /** Highest accepted number of decimal places for the unit price. */
    public static final int MAX_PRICE_SCALE = 2;

    /** Minimum accepted shelf life in months. */
    public static final int MIN_EXPIRY_MONTHS = 0;

    /** Maximum accepted shelf life in months. */
    public static final int MAX_EXPIRY_MONTHS = 120;

    /**
     * Characters accepted inside a snack name.
     *
     * <p>Letters, digits, spaces and the punctuation that regularly appears in a product name are
     * accepted. Everything else - most importantly the characters used to inject markup - is
     * rejected so that a hostile name can never reach the catalogue.</p>
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 &().,'/+\\-%_#:]+$");

    /**
     * Validates the given request.
     *
     * @param request payload to validate, may be {@code null}
     * @return a map of field name to error message, empty when the payload is valid
     */
    public Map<String, String> validate(VeganSnackRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (request == null) {
            fieldErrors.put("snackName", "Snack Name is required");
            fieldErrors.put("snackType", "Snack Type is required");
            fieldErrors.put("quantity", "Quantity is required");
            fieldErrors.put("price", "Price is required");
            fieldErrors.put("expiryInMonths", "Expiry in months is required");
            return fieldErrors;
        }
        validateSnackName(request.getSnackName(), fieldErrors);
        validateSnackType(request.getSnackType(), fieldErrors);
        validateQuantity(request.getQuantity(), fieldErrors);
        validatePrice(request.getPrice(), fieldErrors);
        validateExpiry(request.getExpiryInMonths(), fieldErrors);
        return fieldErrors;
    }

    /**
     * Validates the snack name.
     *
     * @param snackName  name to validate
     * @param fieldErrors collector of the detected problems
     */
    private void validateSnackName(String snackName, Map<String, String> fieldErrors) {
        if (snackName == null || snackName.trim().isEmpty()) {
            fieldErrors.put("snackName", "Snack Name is required");
            return;
        }
        String trimmedName = snackName.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH || trimmedName.length() > MAX_NAME_LENGTH) {
            fieldErrors.put("snackName", "Snack Name must be between 2 and 60 characters");
            return;
        }
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            fieldErrors.put("snackName", "Snack Name contains unsupported characters");
        }
    }

    /**
     * Validates the free text snack type.
     *
     * @param snackType  type to validate
     * @param fieldErrors collector of the detected problems
     */
    private void validateSnackType(String snackType, Map<String, String> fieldErrors) {
        if (snackType == null || snackType.trim().isEmpty()) {
            fieldErrors.put("snackType", "Snack Type is required");
            return;
        }
        if (snackType.trim().length() > MAX_TYPE_LENGTH) {
            fieldErrors.put("snackType", "Snack Type must be 40 characters or less");
        }
    }

    /**
     * Validates the stock quantity.
     *
     * @param quantity   quantity to validate
     * @param fieldErrors collector of the detected problems
     */
    private void validateQuantity(Integer quantity, Map<String, String> fieldErrors) {
        if (quantity == null) {
            fieldErrors.put("quantity", "Quantity is required");
            return;
        }
        if (quantity < MIN_QUANTITY) {
            fieldErrors.put("quantity", "Quantity must be at least 1");
            return;
        }
        if (quantity > MAX_QUANTITY) {
            fieldErrors.put("quantity", "Quantity must be 100000 or less");
        }
    }

    /**
     * Validates the unit price.
     *
     * @param price       price to validate
     * @param fieldErrors collector of the detected problems
     */
    private void validatePrice(BigDecimal price, Map<String, String> fieldErrors) {
        if (price == null) {
            fieldErrors.put("price", "Price is required");
            return;
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            fieldErrors.put("price", "Price must be a positive number");
            return;
        }
        if (price.compareTo(new BigDecimal("1000000")) > 0) {
            fieldErrors.put("price", "Price must be 1000000 or less");
            return;
        }
        if (price.stripTrailingZeros().scale() > MAX_PRICE_SCALE) {
            fieldErrors.put("price", "Price must have at most 2 decimal places");
        }
    }

    /**
     * Validates the remaining shelf life.
     *
     * @param expiryInMonths shelf life to validate
     * @param fieldErrors    collector of the detected problems
     */
    private void validateExpiry(Integer expiryInMonths, Map<String, String> fieldErrors) {
        if (expiryInMonths == null) {
            fieldErrors.put("expiryInMonths", "Expiry in months is required");
            return;
        }
        if (expiryInMonths < MIN_EXPIRY_MONTHS) {
            fieldErrors.put("expiryInMonths", "Expiry must be a non-negative number");
            return;
        }
        if (expiryInMonths > MAX_EXPIRY_MONTHS) {
            fieldErrors.put("expiryInMonths", "Expiry must be 120 months or less");
        }
    }
}
