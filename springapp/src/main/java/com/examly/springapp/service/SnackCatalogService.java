package com.examly.springapp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examly.springapp.dto.response.SnackCatalogStatsResponse;
import com.examly.springapp.dto.response.VeganSnackResponse;

/**
 * Read oriented service behind the catalogue endpoints.
 *
 * <p>The service groups the browsing operations of the catalogue and keeps the controllers free
 * of any catalogue logic. All figures are delegated to {@link VeganSnackService}, which owns the
 * single source of truth for the catalogue.</p>
 */
@Service
public class SnackCatalogService {

    /** Name reported by the health endpoint. */
    public static final String SERVICE_NAME = "springapp";

    /** Status reported while the application is up and running. */
    public static final String STATUS_UP = "UP";

    /** Core service of the vegan snack catalogue. */
    private final VeganSnackService veganSnackService;

    /**
     * Creates the service.
     *
     * @param veganSnackService core service of the vegan snack catalogue
     */
    public SnackCatalogService(VeganSnackService veganSnackService) {
        this.veganSnackService = veganSnackService;
    }

    /**
     * Returns the catalogue, optionally narrowed down by a type filter or a search term.
     *
     * @param type   optional type filter
     * @param search optional search term
     * @return the matching catalogue entries, never {@code null}
     */
    @Transactional(readOnly = true)
    public List<VeganSnackResponse> findSnacks(String type, String search) {
        return veganSnackService.search(type, search);
    }

    /**
     * Returns the aggregated catalogue figures.
     *
     * @return the aggregated catalogue figures
     */
    @Transactional(readOnly = true)
    public SnackCatalogStatsResponse buildStats() {
        return veganSnackService.getCatalogStats();
    }

    /**
     * Returns the health of the service together with the current size of the catalogue.
     *
     * @return the health payload
     */
    @Transactional(readOnly = true)
    public Map<String, Object> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", STATUS_UP);
        health.put("service", SERVICE_NAME);
        health.put("snackCount", veganSnackService.countSnacks());
        return health;
    }
}
