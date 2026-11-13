package com.examly.springapp.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representation of a catalogue entry returned to the client.
 *
 * <p>Besides the persisted values the response carries the derived information a client needs to
 * render a catalogue without any further calculation: the resolved category, the expiry date, the
 * freshness status, the price band and the total inventory value.</p>
 */
public class VeganSnackResponse {

    /** Identifier of the catalogue entry. */
    private Long id;

    /** Name of the product. */
    private String snackName;

    /** Free text description of the product category. */
    private String snackType;

    /** Resolved catalogue category. */
    private String category;

    /** Number of units held in stock. */
    private Integer quantity;

    /** Price of a single unit. */
    private BigDecimal price;

    /** Remaining shelf life expressed in months. */
    private Integer expiryInMonths;

    /** Date on which the product expires. */
    private LocalDate expiryDate;

    /** Freshness classification derived from the shelf life. */
    private String freshnessStatus;

    /** Commercial band derived from the unit price. */
    private String priceBand;

    /** Total value of the stock position. */
    private BigDecimal totalValue;

    /** Indicator that the product belongs to the vegan range. */
    private boolean isVegan;

    /** Point in time at which the catalogue entry was created. */
    private LocalDateTime createdAt;

    /**
     * Creates an empty response.
     */
    public VeganSnackResponse() {
        // Required for JSON serialisation.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getFreshnessStatus() {
        return freshnessStatus;
    }

    public void setFreshnessStatus(String freshnessStatus) {
        this.freshnessStatus = freshnessStatus;
    }

    public String getPriceBand() {
        return priceBand;
    }

    public void setPriceBand(String priceBand) {
        this.priceBand = priceBand;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public boolean getIsVegan() {
        return isVegan;
    }

    public void setIsVegan(boolean vegan) {
        this.isVegan = vegan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
