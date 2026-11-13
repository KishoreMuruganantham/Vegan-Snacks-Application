package com.examly.springapp.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated view of the catalogue used by the dashboard.
 *
 * <p>The response carries the totals of the whole catalogue together with a per category
 * breakdown. All monetary figures are rounded to two decimal places.</p>
 */
public class SnackCatalogStatsResponse {

    /** Number of catalogue entries. */
    private long totalSnacks;

    /** Sum of the quantities held across all catalogue entries. */
    private long totalQuantity;

    /** Sum of the inventory values across all catalogue entries. */
    private BigDecimal totalInventoryValue;

    /** Average unit price across all catalogue entries. */
    private BigDecimal averagePrice;

    /** Number of entries that expire within the next two months. */
    private long expiringSoonCount;

    /** Number of entries that are already expired. */
    private long expiredCount;

    /** Breakdown of the catalogue per category. */
    private List<CategoryStat> byCategory = new ArrayList<>();

    /**
     * Creates an empty response.
     */
    public SnackCatalogStatsResponse() {
        // Required for JSON serialisation.
    }

    public long getTotalSnacks() {
        return totalSnacks;
    }

    public void setTotalSnacks(long totalSnacks) {
        this.totalSnacks = totalSnacks;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public long getExpiringSoonCount() {
        return expiringSoonCount;
    }

    public void setExpiringSoonCount(long expiringSoonCount) {
        this.expiringSoonCount = expiringSoonCount;
    }

    public long getExpiredCount() {
        return expiredCount;
    }

    public void setExpiredCount(long expiredCount) {
        this.expiredCount = expiredCount;
    }

    public List<CategoryStat> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<CategoryStat> byCategory) {
        this.byCategory = byCategory;
    }

    /**
     * Aggregated figures of a single catalogue category.
     */
    public static class CategoryStat {

        /** Name of the category. */
        private String category;

        /** Number of catalogue entries in the category. */
        private long count;

        /** Sum of the quantities held in the category. */
        private long totalQuantity;

        /** Sum of the inventory values in the category. */
        private BigDecimal totalValue;

        /**
         * Creates an empty breakdown entry.
         */
        public CategoryStat() {
            // Required for JSON serialisation.
        }

        /**
         * Creates a fully initialised breakdown entry.
         *
         * @param category      name of the category
         * @param count         number of catalogue entries
         * @param totalQuantity sum of the quantities held
         * @param totalValue    sum of the inventory values
         */
        public CategoryStat(String category, long count, long totalQuantity, BigDecimal totalValue) {
            this.category = category;
            this.count = count;
            this.totalQuantity = totalQuantity;
            this.totalValue = totalValue;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(long totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }
    }
}
