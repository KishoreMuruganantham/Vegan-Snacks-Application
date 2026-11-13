package com.examly.springapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.SnackCatalogStatsResponse;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.exception.DuplicateSnackException;
import com.examly.springapp.exception.InvalidSnackException;
import com.examly.springapp.exception.SnackNotFoundException;
import com.examly.springapp.mapper.VeganSnackMapper;
import com.examly.springapp.model.FreshnessStatus;
import com.examly.springapp.model.SnackCategory;
import com.examly.springapp.model.VeganSnack;
import com.examly.springapp.repository.VeganSnackRepo;
import com.examly.springapp.utility.SnackPricingPolicy;
import com.examly.springapp.utility.SnackValidator;

/**
 * Core service of the vegan snack catalogue.
 *
 * <p>The service owns the write path of the catalogue. Every submission is validated, normalised
 * and checked for duplicates before it is persisted. Read operations return the transport
 * representation of the catalogue entries and the aggregated dashboard figures.</p>
 */
@Service
public class VeganSnackService {

    /** Logger used to trace the catalogue operations. */
    private static final Logger logger = LoggerFactory.getLogger(VeganSnackService.class);

    /** Scale used for the monetary figures of the dashboard. */
    private static final int MONEY_SCALE = 2;

    /** Data access gateway of the catalogue. */
    private final VeganSnackRepo veganSnackRepo;

    /** Validator applied to every submitted payload. */
    private final SnackValidator snackValidator;

    /** Policy providing the derived pricing and freshness information. */
    private final SnackPricingPolicy snackPricingPolicy;

    /** Converter between catalogue records and their transport representation. */
    private final VeganSnackMapper veganSnackMapper;

    /**
     * Creates the service.
     *
     * @param veganSnackRepo    data access gateway of the catalogue
     * @param snackValidator    validator applied to every submitted payload
     * @param snackPricingPolicy policy providing the derived pricing and freshness information
     * @param veganSnackMapper  converter between catalogue records and their transport representation
     */
    public VeganSnackService(VeganSnackRepo veganSnackRepo, SnackValidator snackValidator,
                              SnackPricingPolicy snackPricingPolicy, VeganSnackMapper veganSnackMapper) {
        this.veganSnackRepo = veganSnackRepo;
        this.snackValidator = snackValidator;
        this.snackPricingPolicy = snackPricingPolicy;
        this.veganSnackMapper = veganSnackMapper;
    }

    /**
     * Validates and stores a new catalogue entry.
     *
     * @param request payload submitted by the client
     * @return the stored catalogue entry
     * @throws InvalidSnackException  when the payload violates the catalogue rules
     * @throws DuplicateSnackException when the name is already used by another entry
     */
    @Transactional
    public VeganSnackResponse addVeganSnack(VeganSnackRequest request) {
        VeganSnackRequest normalisedRequest = normalise(validatedRequest(request));
        if (veganSnackRepo.existsBySnackNameIgnoreCase(normalisedRequest.getSnackName())) {
            throw new DuplicateSnackException(normalisedRequest.getSnackName());
        }
        SnackCategory category = SnackCategory.resolve(normalisedRequest.getSnackType());
        VeganSnack entity = veganSnackMapper.toEntity(normalisedRequest, category);
        VeganSnack savedEntity = veganSnackRepo.save(entity);
        logger.info("Added vegan snack '{}' with category {}", savedEntity.getSnackName(),
                savedEntity.getCategory());
        return veganSnackMapper.toResponse(savedEntity);
    }

    /**
     * Returns the complete catalogue, newest entry first.
     *
     * @return the catalogue entries, never {@code null}
     */
    @Transactional(readOnly = true)
    public List<VeganSnackResponse> getAllVeganSnacks() {
        return toResponses(veganSnackRepo.findAllByOrderByIdDesc());
    }

    /**
     * Returns a single catalogue entry.
     *
     * @param id identifier of the requested entry
     * @return the requested catalogue entry
     * @throws SnackNotFoundException when no entry carries the given identifier
     */
    @Transactional(readOnly = true)
    public VeganSnackResponse getSnackById(Long id) {
        return veganSnackMapper.toResponse(requireSnack(id));
    }

    /**
     * Validates and applies an update to an existing catalogue entry.
     *
     * @param id      identifier of the entry to update
     * @param request payload submitted by the client
     * @return the updated catalogue entry
     * @throws InvalidSnackException  when the payload violates the catalogue rules
     * @throws SnackNotFoundException when no entry carries the given identifier
     * @throws DuplicateSnackException when the name is already used by another entry
     */
    @Transactional
    public VeganSnackResponse updateVeganSnack(Long id, VeganSnackRequest request) {
        VeganSnack entity = requireSnack(id);
        VeganSnackRequest normalisedRequest = normalise(validatedRequest(request));
        if (veganSnackRepo.existsBySnackNameIgnoreCaseAndIdNot(normalisedRequest.getSnackName(), id)) {
            throw new DuplicateSnackException(normalisedRequest.getSnackName());
        }
        SnackCategory category = SnackCategory.resolve(normalisedRequest.getSnackType());
        veganSnackMapper.applyToEntity(entity, normalisedRequest, category);
        VeganSnack savedEntity = veganSnackRepo.save(entity);
        logger.info("Updated vegan snack '{}' with id {}", savedEntity.getSnackName(), savedEntity.getId());
        return veganSnackMapper.toResponse(savedEntity);
    }

    /**
     * Removes a catalogue entry.
     *
     * @param id identifier of the entry to remove
     * @throws SnackNotFoundException when no entry carries the given identifier
     */
    @Transactional
    public void deleteVeganSnack(Long id) {
        VeganSnack entity = requireSnack(id);
        veganSnackRepo.delete(entity);
        logger.info("Removed vegan snack '{}' with id {}", entity.getSnackName(), entity.getId());
    }

    /**
     * Searches the catalogue.
     *
     * <p>A type filter narrows the catalogue down to a single category. The filter accepts both the
     * free text description used in the catalogue and the name of a catalogue category. A search
     * term matches the name and the free text type of the entries.</p>
     *
     * @param type   optional type filter
     * @param search optional search term
     * @return the matching catalogue entries, never {@code null}
     */
    @Transactional(readOnly = true)
    public List<VeganSnackResponse> search(String type, String search) {
        List<VeganSnack> entities;
        if (hasText(type)) {
            String typeFragment = type.trim();
            SnackCategory category = SnackCategory.resolve(typeFragment);
            if (category == SnackCategory.OTHERS) {
                entities = veganSnackRepo.findBySnackTypeContainingIgnoreCaseOrderByIdDesc(typeFragment);
            } else {
                entities = veganSnackRepo.findByCategoryOrderByIdDesc(category);
            }
        } else if (hasText(search)) {
            String searchFragment = search.trim();
            entities = veganSnackRepo.findBySnackNameContainingIgnoreCaseOrSnackTypeContainingIgnoreCaseOrderByIdDesc(
                    searchFragment, searchFragment);
        } else {
            entities = veganSnackRepo.findAllByOrderByIdDesc();
        }
        return toResponses(entities);
    }

    /**
     * Aggregates the catalogue into the figures used by the dashboard.
     *
     * @return the aggregated catalogue figures
     */
    @Transactional(readOnly = true)
    public SnackCatalogStatsResponse getCatalogStats() {
        List<VeganSnack> entities = veganSnackRepo.findAllByOrderByIdDesc();
        SnackCatalogStatsResponse stats = new SnackCatalogStatsResponse();
        BigDecimal inventoryValue = BigDecimal.ZERO;
        BigDecimal unitPriceSum = BigDecimal.ZERO;
        long totalQuantity = 0L;
        long expiringSoonCount = 0L;
        long expiredCount = 0L;
        Map<SnackCategory, CategoryAccumulator> categoryCounters = new LinkedHashMap<>();

        for (VeganSnack entity : entities) {
            int quantity = entity.getQuantity() == null ? 0 : entity.getQuantity();
            BigDecimal price = entity.getPrice() == null ? BigDecimal.ZERO : entity.getPrice();
            BigDecimal value = snackPricingPolicy.inventoryValue(price, quantity);
            inventoryValue = inventoryValue.add(value);
            unitPriceSum = unitPriceSum.add(price);
            totalQuantity += quantity;

            FreshnessStatus freshnessStatus = snackPricingPolicy.freshnessStatus(entity.getExpiryInMonths());
            if (freshnessStatus == FreshnessStatus.EXPIRING_SOON) {
                expiringSoonCount++;
            } else if (freshnessStatus == FreshnessStatus.EXPIRED) {
                expiredCount++;
            }

            SnackCategory category = entity.getCategory() == null ? SnackCategory.OTHERS : entity.getCategory();
            CategoryAccumulator accumulator = categoryCounters.computeIfAbsent(category,
                    key -> new CategoryAccumulator());
            accumulator.add(quantity, value);
        }

        stats.setTotalSnacks(entities.size());
        stats.setTotalQuantity(totalQuantity);
        stats.setTotalInventoryValue(inventoryValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        stats.setAveragePrice(averagePrice(unitPriceSum, entities.size()));
        stats.setExpiringSoonCount(expiringSoonCount);
        stats.setExpiredCount(expiredCount);
        stats.setByCategory(buildCategoryBreakdown(categoryCounters));
        return stats;
    }

    /**
     * Returns the number of catalogue entries.
     *
     * @return the number of stored catalogue entries
     */
    @Transactional(readOnly = true)
    public long countSnacks() {
        return veganSnackRepo.count();
    }

    /**
     * Validates a payload and rejects it when the rules are violated.
     *
     * @param request payload submitted by the client
     * @return the validated payload
     * @throws InvalidSnackException when the payload violates the catalogue rules
     */
    private VeganSnackRequest validatedRequest(VeganSnackRequest request) {
        Map<String, String> fieldErrors = snackValidator.validate(request);
        if (!fieldErrors.isEmpty()) {
            throw new InvalidSnackException(fieldErrors);
        }
        return request;
    }

    /**
     * Trims the submitted values and collapses repeated whitespace.
     *
     * <p>The capitalisation entered by the user is preserved because the catalogue stores the
     * product name as it was written.</p>
     *
     * @param request validated payload
     * @return a normalised copy of the payload
     */
    private VeganSnackRequest normalise(VeganSnackRequest request) {
        VeganSnackRequest normalised = new VeganSnackRequest();
        normalised.setSnackName(collapseWhitespace(request.getSnackName()));
        normalised.setSnackType(collapseWhitespace(request.getSnackType()));
        normalised.setQuantity(request.getQuantity());
        normalised.setPrice(request.getPrice());
        normalised.setExpiryInMonths(request.getExpiryInMonths());
        return normalised;
    }

    /**
     * Collapses repeated whitespace and trims the given value.
     *
     * @param value value to normalise
     * @return the normalised value
     */
    private String collapseWhitespace(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Loads a catalogue entry.
     *
     * @param id identifier of the requested entry
     * @return the requested catalogue entry
     * @throws SnackNotFoundException when no entry carries the given identifier
     */
    private VeganSnack requireSnack(Long id) {
        return veganSnackRepo.findById(id).orElseThrow(() -> new SnackNotFoundException(id));
    }

    /**
     * Converts catalogue records into their transport representation.
     *
     * @param entities records to convert
     * @return the converted records, never {@code null}
     */
    private List<VeganSnackResponse> toResponses(List<VeganSnack> entities) {
        List<VeganSnackResponse> responses = new ArrayList<>();
        for (VeganSnack entity : entities) {
            responses.add(veganSnackMapper.toResponse(entity));
        }
        return responses;
    }

    /**
     * Calculates the average unit price of the catalogue.
     *
     * @param unitPriceSum sum of the unit prices
     * @param snackCount   number of catalogue entries
     * @return the average unit price
     */
    private BigDecimal averagePrice(BigDecimal unitPriceSum, int snackCount) {
        if (snackCount == 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return unitPriceSum.divide(BigDecimal.valueOf(snackCount), MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Builds the per category breakdown of the catalogue.
     *
     * @param categoryCounters counters collected per category
     * @return the breakdown entries ordered by the catalogue taxonomy
     */
    private List<SnackCatalogStatsResponse.CategoryStat> buildCategoryBreakdown(
            Map<SnackCategory, CategoryAccumulator> categoryCounters) {
        List<SnackCatalogStatsResponse.CategoryStat> breakdown = new ArrayList<>();
        for (SnackCategory category : SnackCategory.values()) {
            CategoryAccumulator accumulator = categoryCounters.get(category);
            if (accumulator == null) {
                continue;
            }
            breakdown.add(new SnackCatalogStatsResponse.CategoryStat(category.name(), accumulator.getCount(),
                    accumulator.getTotalQuantity(), accumulator.getTotalValue()));
        }
        return breakdown;
    }

    /**
     * Collects the figures of a single catalogue category.
     */
    private static final class CategoryAccumulator {

        /** Number of catalogue entries in the category. */
        private long count;

        /** Sum of the quantities held in the category. */
        private long totalQuantity;

        /** Sum of the inventory values in the category. */
        private BigDecimal totalValue = BigDecimal.ZERO;

        /**
         * Adds the figures of a single catalogue entry.
         *
         * @param quantity number of units held
         * @param value    inventory value of the entry
         */
        void add(int quantity, BigDecimal value) {
            count++;
            totalQuantity += quantity;
            totalValue = totalValue.add(value);
        }

        long getCount() {
            return count;
        }

        long getTotalQuantity() {
            return totalQuantity;
        }

        BigDecimal getTotalValue() {
            return totalValue.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * Checks whether a value carries content.
     *
     * @param value value to inspect
     * @return {@code true} when the value is neither {@code null} nor blank
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
