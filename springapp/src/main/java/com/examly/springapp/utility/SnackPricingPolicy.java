package com.examly.springapp.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.examly.springapp.model.FreshnessStatus;

/**
 * Pricing and valuation rules applied to every catalogue entry.
 *
 * <p>The policy is a pure function of its input, the only state it holds is the {@link Clock} used
 * to resolve the expiry date. Injecting the clock keeps the class deterministic in tests while
 * still resolving against the current date at runtime.</p>
 */
@Component
public class SnackPricingPolicy {

    /** Band applied to products priced below one hundred. */
    public static final String BUDGET_BAND = "BUDGET";

    /** Band applied to products priced between one hundred and five hundred. */
    public static final String STANDARD_BAND = "STANDARD";

    /** Band applied to products priced between five hundred and two thousand. */
    public static final String PREMIUM_BAND = "PREMIUM";

    /** Band applied to products priced at two thousand or more. */
    public static final String ARTISAN_BAND = "ARTISAN";

    /** Lower bound of the standard band. */
    private static final BigDecimal STANDARD_LIMIT = new BigDecimal("100");

    /** Lower bound of the premium band. */
    private static final BigDecimal PREMIUM_LIMIT = new BigDecimal("500");

    /** Lower bound of the artisan band. */
    private static final BigDecimal ARTISAN_LIMIT = new BigDecimal("2000");

    /** Scale used for every monetary result. */
    private static final int MONEY_SCALE = 2;

    /** Clock used to resolve the current date. */
    private final Clock clock;

    /**
     * Creates a policy bound to the system clock.
     */
    public SnackPricingPolicy() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a policy bound to the given clock.
     *
     * @param clock clock used to resolve the current date
     */
    public SnackPricingPolicy(Clock clock) {
        this.clock = clock;
    }

    /**
     * Resolves the price band of a product.
     *
     * @param price price of a single unit
     * @return the name of the matching band
     */
    public String priceBand(BigDecimal price) {
        if (price == null) {
            return BUDGET_BAND;
        }
        if (price.compareTo(STANDARD_LIMIT) < 0) {
            return BUDGET_BAND;
        }
        if (price.compareTo(PREMIUM_LIMIT) < 0) {
            return STANDARD_BAND;
        }
        if (price.compareTo(ARTISAN_LIMIT) < 0) {
            return PREMIUM_BAND;
        }
        return ARTISAN_BAND;
    }

    /**
     * Resolves the freshness status of a product from its remaining shelf life.
     *
     * @param expiryInMonths remaining shelf life expressed in months
     * @return the matching freshness status
     */
    public FreshnessStatus freshnessStatus(int expiryInMonths) {
        if (expiryInMonths <= 0) {
            return FreshnessStatus.SHELF_STABLE;
        }
        if (expiryInMonths <= 2) {
            return FreshnessStatus.EXPIRING_SOON;
        }
        if (expiryInMonths <= 11) {
            return FreshnessStatus.FRESH;
        }
        return FreshnessStatus.SHELF_STABLE;
    }

    /**
     * Resolves the date on which a product expires.
     *
     * @param expiryInMonths remaining shelf life expressed in months
     * @return the expiry date calculated from the current date
     */
    public LocalDate expiryDate(int expiryInMonths) {
        return LocalDate.now(clock).plusMonths(expiryInMonths);
    }

    /**
     * Calculates the inventory value of a stock position.
     *
     * @param price    price of a single unit
     * @param quantity number of units held in stock
     * @return the total value rounded to two decimal places
     */
    public BigDecimal inventoryValue(BigDecimal price, int quantity) {
        if (price == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return price.multiply(BigDecimal.valueOf(quantity)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Normalises a monetary amount to the scale used by the catalogue.
     *
     * @param amount amount to normalise
     * @return the amount scaled to two decimal places
     */
    public BigDecimal normalise(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
