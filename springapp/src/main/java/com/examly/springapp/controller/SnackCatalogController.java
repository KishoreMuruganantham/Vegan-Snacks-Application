package com.examly.springapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.SnackCatalogStatsResponse;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.service.SnackCatalogService;
import com.examly.springapp.service.VeganSnackService;

/**
 * Browsing and maintenance endpoints of the catalogue.
 *
 * <p>The endpoints live below the {@code /api/v1} namespace and complement the form oriented
 * endpoints of {@link VeganSnackController}.</p>
 */
@RestController
public class SnackCatalogController {

    /** Read oriented service behind the catalogue endpoints. */
    private final SnackCatalogService snackCatalogService;

    /** Core service of the vegan snack catalogue. */
    private final VeganSnackService veganSnackService;

    /**
     * Creates the controller.
     *
     * @param snackCatalogService read oriented service behind the catalogue endpoints
     * @param veganSnackService   core service of the vegan snack catalogue
     */
    public SnackCatalogController(SnackCatalogService snackCatalogService, VeganSnackService veganSnackService) {
        this.snackCatalogService = snackCatalogService;
        this.veganSnackService = veganSnackService;
    }

    /**
     * Returns the catalogue, optionally narrowed down by a type filter or a search term.
     *
     * @param type   optional type filter
     * @param search optional search term
     * @return the matching catalogue entries, never {@code null}
     */
    @GetMapping("/api/v1/snacks")
    public ResponseEntity<List<VeganSnackResponse>> listSnacks(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(snackCatalogService.findSnacks(type, search));
    }

    /**
     * Returns a single catalogue entry.
     *
     * @param id identifier of the requested entry
     * @return the requested catalogue entry
     */
    @GetMapping("/api/v1/snacks/{id}")
    public ResponseEntity<VeganSnackResponse> getSnack(@PathVariable Long id) {
        return ResponseEntity.ok(veganSnackService.getSnackById(id));
    }

    /**
     * Applies an update to an existing catalogue entry.
     *
     * @param id      identifier of the entry to update
     * @param request payload submitted by the client
     * @return the updated catalogue entry
     */
    @PutMapping("/api/v1/snacks/{id}")
    public ResponseEntity<VeganSnackResponse> updateSnack(@PathVariable Long id,
                                                           @RequestBody VeganSnackRequest request) {
        return ResponseEntity.ok(veganSnackService.updateVeganSnack(id, request));
    }

    /**
     * Removes a catalogue entry.
     *
     * @param id identifier of the entry to remove
     * @return an empty response with status {@code 204 No Content}
     */
    @DeleteMapping("/api/v1/snacks/{id}")
    public ResponseEntity<Void> deleteSnack(@PathVariable Long id) {
        veganSnackService.deleteVeganSnack(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Returns the aggregated catalogue figures.
     *
     * @return the aggregated catalogue figures
     */
    @GetMapping("/api/v1/snacks/stats")
    public ResponseEntity<SnackCatalogStatsResponse> stats() {
        return ResponseEntity.ok(snackCatalogService.buildStats());
    }

    /**
     * Returns the health of the service.
     *
     * @return the health payload
     */
    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(snackCatalogService.health());
    }
}
