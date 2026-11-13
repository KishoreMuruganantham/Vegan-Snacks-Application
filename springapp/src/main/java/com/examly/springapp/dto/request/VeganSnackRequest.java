package com.examly.springapp.dto.request;

import java.math.BigDecimal;

/**
 * Payload accepted by the catalogue write endpoints.
 *
 * <p>The type mirrors the form captured by the client. Every field is optional at the transport
 * level; the semantic checks are performed by the validator inside the service layer so that the
 * client receives a complete list of problems with a single response.</p>
 */
public class VeganSnackRequest {

    /** Name of the product. */
    private String snackName;

    /** Free text description of the product category. */
    private String snackType;

    /** Number of units held in stock. */
    private Integer quantity;

    /** Price of a single unit. */
    private BigDecimal price;

    /** Remaining shelf life expressed in months. */
    private Integer expiryInMonths;

    /**
     * Creates an empty payload.
     */
    public VeganSnackRequest() {
        // Required for JSON deserialisation.
    }

    /**
     * Creates a fully initialised payload.
     *
     * @param snackName      name of the product
     * @param snackType      free text type description
     * @param quantity       number of units held in stock
     * @param price          price of a single unit
     * @param expiryInMonths remaining shelf life in months
     */
    public VeganSnackRequest(String snackName, String snackType, Integer quantity, BigDecimal price,
                             Integer expiryInMonths) {
        this.snackName = snackName;
        this.snackType = snackType;
        this.quantity = quantity;
        this.price = price;
        this.expiryInMonths = expiryInMonths;
    }

    public String getSnackName() {
        return snackName;
    }

    public void setSnackName(String snackName) {
        this.snackName = snackName;
    }

    public String getSnackType() {
        return snackType;
    }

    public void setSnackType(String snackType) {
        this.snackType = snackType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getExpiryInMonths() {
        return expiryInMonths;
    }

    public void setExpiryInMonths(Integer expiryInMonths) {
        this.expiryInMonths = expiryInMonths;
    }
}
