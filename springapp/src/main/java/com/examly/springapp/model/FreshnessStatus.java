package com.examly.springapp.model;

/**
 * Freshness classification derived from the remaining shelf life of a snack.
 *
 * <p>The status is derived from the number of months the product stays edible and is therefore
 * never stored in the database. It is recalculated whenever a snack is read or written.</p>
 */
public enum FreshnessStatus {

    /** The product is past its expiry date and must not be sold. */
    EXPIRED,

    /** The product expires within the next two months and should be prioritised. */
    EXPIRING_SOON,

    /** The product keeps its quality for the coming months. */
    FRESH,

    /** The product is shelf stable and can be stored for a long period. */
    SHELF_STABLE
}
