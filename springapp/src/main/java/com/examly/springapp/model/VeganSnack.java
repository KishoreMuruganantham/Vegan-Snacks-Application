package com.examly.springapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Persistent representation of a vegan snack in the catalogue.
 *
 * <p>The entity only carries persisted state. Derived information such as the price band, the
 * freshness status or the inventory value is derived by the pricing policy and the mapper, which
 * keeps the table free of redundant columns.</p>
 */
@Entity
@Table(name = "vegan_snacks")
public class VeganSnack {

    /** Surrogate key assigned by the database on insert. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Name of the product as entered in the catalogue. */
    @Column(name = "snack_name", nullable = false, length = 60)
    private String snackName;

    /** Free text description of the product category as entered by the user. */
    @Column(name = "snack_type", nullable = false, length = 40)
    private String snackType;

    /** Catalogue category resolved from the free text snack type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private SnackCategory category;

    /** Number of units currently held in stock. */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Price of a single unit in the store currency. */
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Remaining shelf life of the product expressed in months. */
    @Column(name = "expiry_in_months", nullable = false)
    private Integer expiryInMonths;

    /** Point in time at which the catalogue record was created. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Required no argument constructor used by the persistence provider.
     */
    public VeganSnack() {
        // Required by the JPA specification.
    }

    /**
     * Creates a fully initialised catalogue record.
     *
     * @param snackName      name of the product
     * @param snackType      free text type description
     * @param category       resolved catalogue category
     * @param quantity       number of units in stock
     * @param price          price of a single unit
     * @param expiryInMonths remaining shelf life in months
     */
    public VeganSnack(String snackName, String snackType, SnackCategory category, Integer quantity,
                      BigDecimal price, Integer expiryInMonths) {
        this.snackName = snackName;
        this.snackType = snackType;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.expiryInMonths = expiryInMonths;
    }

    /**
     * Stamps the creation timestamp the first time the record is persisted.
     */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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

    public SnackCategory getCategory() {
        return category;
    }

    public void setCategory(SnackCategory category) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VeganSnack)) {
            return false;
        }
        VeganSnack that = (VeganSnack) other;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "VeganSnack{id=" + id + ", snackName='" + snackName + "'}";
    }
}
