package com.examly.springapp.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.exception.DuplicateSnackException;
import com.examly.springapp.exception.InvalidSnackException;
import com.examly.springapp.service.VeganSnackService;

/**
 * Web layer tests of the catalogue form endpoints.
 *
 * <p>The tests verify the response codes, the payload structure and the error handling that the
 * client relies on.</p>
 */
@WebMvcTest(VeganSnackController.class)
class VeganSnackControllerTests {

    /** Test client used to exercise the endpoints. */
    @Autowired
    private MockMvc mockMvc;

    /** Core service of the vegan snack catalogue. */
    @MockBean
    private VeganSnackService veganSnackService;

    /**
     * A valid submission is accepted and the created entry is returned.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void addVeganSnack_withValidPayload_returns200AndCreatedSnack() throws Exception {
        when(veganSnackService.addVeganSnack(any(VeganSnackRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/addVeganSnack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.snackName").value("Roasted Almonds"))
                .andExpect(jsonPath("$.snackType").value("Nuts and Seeds"))
                .andExpect(jsonPath("$.category").value("NUTS_AND_SEEDS"))
                .andExpect(jsonPath("$.quantity").value(12))
                .andExpect(jsonPath("$.price").value(149.50))
                .andExpect(jsonPath("$.expiryInMonths").value(6))
                .andExpect(jsonPath("$.expiryDate").value("2027-05-13"))
                .andExpect(jsonPath("$.freshnessStatus").value("FRESH"))
                .andExpect(jsonPath("$.priceBand").value("STANDARD"))
                .andExpect(jsonPath("$.totalValue").value(1794.00))
                .andExpect(jsonPath("$.isVegan").value(true))
                .andExpect(jsonPath("$.createdAt").value("2026-11-13T05:20:31.123"));
    }

    /**
     * An invalid submission is rejected with a message for every faulty field.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void addVeganSnack_withInvalidPayload_returns400AndFieldErrors() throws Exception {
        when(veganSnackService.addVeganSnack(any(VeganSnackRequest.class)))
                .thenThrow(new InvalidSnackException(Map.of(
                        "snackName", "Snack Name is required",
                        "snackType", "Snack Type is required",
                        "quantity", "Quantity must be at least 1",
                        "price", "Price must be a positive number",
                        "expiryInMonths", "Expiry must be a non-negative number")));

        mockMvc.perform(post("/addVeganSnack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Snack details are invalid."))
                .andExpect(jsonPath("$.fieldErrors.snackName").value("Snack Name is required"))
                .andExpect(jsonPath("$.fieldErrors.snackType").value("Snack Type is required"))
                .andExpect(jsonPath("$.fieldErrors.quantity").value("Quantity must be at least 1"))
                .andExpect(jsonPath("$.fieldErrors.price").value("Price must be a positive number"))
                .andExpect(jsonPath("$.fieldErrors.expiryInMonths").value("Expiry must be a non-negative number"));
    }

    /**
     * A name that is already used by another entry is reported as a conflict.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void addVeganSnack_withExistingName_returns409AndFieldError() throws Exception {
        when(veganSnackService.addVeganSnack(any(VeganSnackRequest.class)))
                .thenThrow(new DuplicateSnackException("Roasted Almonds"));

        mockMvc.perform(post("/addVeganSnack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.fieldErrors.snackName").value("A snack with this name already exists"));
    }

    /**
     * A body that cannot be parsed is rejected with a bad request.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void addVeganSnack_withMalformedPayload_returns400() throws Exception {
        mockMvc.perform(post("/addVeganSnack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    /**
     * The catalogue is returned as a JSON array.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void getAllVeganSnacks_returns200AndJsonArray() throws Exception {
        when(veganSnackService.getAllVeganSnacks())
                .thenReturn(List.of(sampleResponse(), sampleSecondResponse()));

        mockMvc.perform(get("/getAllVeganSnacks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].snackName").value("Roasted Almonds"))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[1].snackName").value("Banana Chips"))
                .andExpect(jsonPath("$[1].id").value(6));
    }

    /**
     * An empty catalogue is returned as an empty JSON array.
     *
     * @throws Exception when the request cannot be executed
     */
    @Test
    void getAllVeganSnacks_withEmptyCatalogue_returns200AndEmptyArray() throws Exception {
        when(veganSnackService.getAllVeganSnacks()).thenReturn(List.of());

        mockMvc.perform(get("/getAllVeganSnacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
     * Builds the response of a stored catalogue entry.
     *
     * @return the response
     */
    private VeganSnackResponse sampleResponse() {
        VeganSnackResponse response = new VeganSnackResponse();
        response.setId(7L);
        response.setSnackName("Roasted Almonds");
        response.setSnackType("Nuts and Seeds");
        response.setCategory("NUTS_AND_SEEDS");
        response.setQuantity(12);
        response.setPrice(new BigDecimal("149.50"));
        response.setExpiryInMonths(6);
        response.setExpiryDate(LocalDate.of(2027, 5, 13));
        response.setFreshnessStatus("FRESH");
        response.setPriceBand("STANDARD");
        response.setTotalValue(new BigDecimal("1794.00"));
        response.setIsVegan(true);
        response.setCreatedAt(LocalDateTime.of(2026, 11, 13, 5, 20, 31, 123_000_000));
        return response;
    }

    /**
     * Builds the response of a second stored catalogue entry.
     *
     * @return the response
     */
    private VeganSnackResponse sampleSecondResponse() {
        VeganSnackResponse response = new VeganSnackResponse();
        response.setId(6L);
        response.setSnackName("Banana Chips");
        response.setSnackType("Chips");
        response.setCategory("CHIPS_AND_CRUNCHIES");
        response.setQuantity(40);
        response.setPrice(new BigDecimal("79.99"));
        response.setExpiryInMonths(4);
        response.setExpiryDate(LocalDate.of(2027, 3, 13));
        response.setFreshnessStatus("FRESH");
        response.setPriceBand("BUDGET");
        response.setTotalValue(new BigDecimal("3199.60"));
        response.setIsVegan(true);
        response.setCreatedAt(LocalDateTime.of(2026, 11, 13, 5, 19, 0));
        return response;
    }

    /**
     * Builds a valid request body.
     *
     * @return the request body
     */
    private String validPayload() {
        return "{\"snackName\":\"Roasted Almonds\",\"snackType\":\"Nuts and Seeds\","
                + "\"quantity\":12,\"price\":149.50,\"expiryInMonths\":6}";
    }

    /**
     * Builds an invalid request body.
     *
     * @return the request body
     */
    private String invalidPayload() {
        return "{\"snackName\":\"\",\"snackType\":\"\",\"quantity\":0,\"price\":-5,\"expiryInMonths\":-1}";
    }
}
