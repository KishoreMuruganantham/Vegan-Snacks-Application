package com.examly.springapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.service.VeganSnackService;

/**
 * Endpoints used by the catalogue form of the client.
 *
 * <p>The paths are part of the agreed contract with the client and therefore stay free of a
 * version prefix. The catalogue endpoints that are not part of that contract are served by
 * {@link SnackCatalogController}.</p>
 */
@RestController
public class VeganSnackController {

    /** Core service of the vegan snack catalogue. */
    private final VeganSnackService veganSnackService;

    /**
     * Creates the controller.
     *
     * @param veganSnackService core service of the vegan snack catalogue
     */
    public VeganSnackController(VeganSnackService veganSnackService) {
        this.veganSnackService = veganSnackService;
    }

    /**
     * Stores a new catalogue entry.
     *
     * @param request payload submitted by the client
     * @return the stored catalogue entry
     */
    @PostMapping("/addVeganSnack")
    public ResponseEntity<VeganSnackResponse> addVeganSnack(@RequestBody VeganSnackRequest request) {
        return ResponseEntity.ok(veganSnackService.addVeganSnack(request));
    }

    /**
     * Returns the complete catalogue, newest entry first.
     *
     * @return the catalogue entries, never {@code null}
     */
    @GetMapping("/getAllVeganSnacks")
    public ResponseEntity<List<VeganSnackResponse>> getAllVeganSnacks() {
        return ResponseEntity.ok(veganSnackService.getAllVeganSnacks());
    }
}
