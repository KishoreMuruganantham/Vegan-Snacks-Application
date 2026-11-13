package com.examly.springapp.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.model.SnackCategory;
import com.examly.springapp.model.VeganSnack;
import com.examly.springapp.utility.SnackPricingPolicy;

/**
 * Converts catalogue records into their transport representation and back.
 *
 * <p>The mapper is the only component that knows how a persisted record is rendered. All derived
 * values are produced by the {@link SnackPricingPolicy} so that the rules stay in a single
 * place.</p>
 */
@Component
public class VeganSnackMapper {

    /** Policy providing the derived pricing and freshness information. */
    private final SnackPricingPolicy snackPricingPolicy;

    /**
     * Creates a mapper.
     *
     * @param snackPricingPolicy policy providing the derived pricing and freshness information
     */
    public VeganSnackMapper(SnackPricingPolicy snackPricingPolicy) {
        this.snackPricingPolicy = snackPricingPolicy;
    }

    /**
     * Converts a request into a new catalogue record.
     *
     * @param request  payload to convert
     * @param category category resolved from the free text snack type
     * @return the new catalogue record
     */
    public VeganSnack toEntity(VeganSnackRequest request, SnackCategory category) {
        VeganSnack entity = new VeganSnack();
        entity.setSnackName(request.getSnackName());
        entity.setSnackType(request.getSnackType());
        entity.setCategory(category);
        entity.setQuantity(request.getQuantity());
        entity.setPrice(normalisePrice(request.getPrice()));
        entity.setExpiryInMonths(request.getExpiryInMonths());
        return entity;
    }

    /**
     * Applies a request to an existing catalogue record.
     *
     * @param entity   record to update
     * @param request  payload to apply
     * @param category category resolved from the free text snack type
     */
    public void applyToEntity(VeganSnack entity, VeganSnackRequest request, SnackCategory category) {
        entity.setSnackName(request.getSnackName());
        entity.setSnackType(request.getSnackType());
        entity.setCategory(category);
        entity.setQuantity(request.getQuantity());
        entity.setPrice(normalisePrice(request.getPrice()));
        entity.setExpiryInMonths(request.getExpiryInMonths());
    }

    /**
     * Converts a catalogue record into its response representation.
     *
     * @param entity record to convert
     * @return the response describing the record
     */
    public VeganSnackResponse toResponse(VeganSnack entity) {
        VeganSnackResponse response = new VeganSnackResponse();
        response.setId(entity.getId());
        response.setSnackName(entity.getSnackName());
        response.setSnackType(entity.getSnackType());
        response.setCategory(entity.getCategory() == null ? null : entity.getCategory().name());
        response.setQuantity(entity.getQuantity());
        response.setPrice(entity.getPrice());
        response.setExpiryInMonths(entity.getExpiryInMonths());
        response.setExpiryDate(snackPricingPolicy.expiryDate(entity.getExpiryInMonths()));
        response.setFreshnessStatus(snackPricingPolicy.freshnessStatus(entity.getExpiryInMonths()).name());
        response.setPriceBand(snackPricingPolicy.priceBand(entity.getPrice()));
        response.setTotalValue(snackPricingPolicy.inventoryValue(entity.getPrice(), entity.getQuantity()));
        response.setIsVegan(true);
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    /**
     * Normalises a price to the scale used by the catalogue.
     *
     * @param price price to normalise
     * @return the normalised price
     */
    private BigDecimal normalisePrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return snackPricingPolicy.normalise(price);
    }
}
