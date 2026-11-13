package com.examly.springapp.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Catalogue taxonomy for vegan snacks.
 *
 * <p>The catalogue stores the snack type as free text captured from the user. Every enum constant
 * therefore owns the set of keywords that maps an arbitrary type description onto a well known
 * catalogue category. Resolution is case insensitive and operates on a trimmed description.</p>
 *
 * <p>Categories are evaluated in a fixed order so that a description matching several keyword
 * groups always produces the same result. More specific groups (chocolate, fruit, nuts) are
 * evaluated before the broader ones (energy bars, baked treats, chips) because compound
 * descriptions such as {@code "Chocolate Chip Cookies"} are more accurately described by the
 * first matching group.</p>
 */
public enum SnackCategory {

    /** Crisped and extruded savoury products. */
    CHIPS_AND_CRUNCHIES,

    /** Nuts, seeds and nut based spreads. */
    NUTS_AND_SEEDS,

    /** Dried and preserved fruit products. */
    FRUIT_SNACKS,

    /** Cocoa based confectionery. */
    CHOCOLATE,

    /** Cereal and protein based bars. */
    ENERGY_BARS,

    /** Baked goods such as crackers, biscuits and cakes. */
    BAKED_TREATS,

    /** Fallback used when no keyword matches the supplied description. */
    OTHERS;

    /**
     * Keyword groups per category. The map preserves the declaration order of the categories.
     */
    private static final Map<SnackCategory, List<String>> KEYWORDS;

    /**
     * Order in which the categories are evaluated during keyword resolution.
     */
    private static final List<SnackCategory> RESOLUTION_ORDER = List.of(
            CHOCOLATE,
            FRUIT_SNACKS,
            NUTS_AND_SEEDS,
            ENERGY_BARS,
            BAKED_TREATS,
            CHIPS_AND_CRUNCHIES);

    static {
        Map<SnackCategory, List<String>> keywords = new LinkedHashMap<>();
        keywords.put(CHOCOLATE, List.of("chocolate", "cocoa"));
        keywords.put(FRUIT_SNACKS, List.of("fruit"));
        keywords.put(NUTS_AND_SEEDS, List.of("nut", "seed"));
        keywords.put(ENERGY_BARS, List.of("bar", "energy"));
        keywords.put(BAKED_TREATS, List.of("cake", "biscuit", "cookie", "bread", "cracker", "baked"));
        keywords.put(CHIPS_AND_CRUNCHIES, List.of("chip", "crunch", "extruded"));
        KEYWORDS = Collections.unmodifiableMap(keywords);
    }

    /**
     * Resolves the catalogue category for a free text snack type.
     *
     * @param snackType free text description supplied by the user, may be {@code null}
     * @return the matching category or {@link #OTHERS} when no keyword matches
     */
    public static SnackCategory resolve(String snackType) {
        if (snackType == null) {
            return OTHERS;
        }
        String normalisedType = snackType.trim().toLowerCase(Locale.ROOT);
        if (normalisedType.isEmpty()) {
            return OTHERS;
        }
        for (SnackCategory category : RESOLUTION_ORDER) {
            for (String keyword : KEYWORDS.get(category)) {
                if (normalisedType.contains(keyword)) {
                    return category;
                }
            }
        }
        return OTHERS;
    }
}
