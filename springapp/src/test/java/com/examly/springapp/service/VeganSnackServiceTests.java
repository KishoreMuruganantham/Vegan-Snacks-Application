package com.examly.springapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.dto.response.SnackCatalogStatsResponse;
import com.examly.springapp.dto.response.VeganSnackResponse;
import com.examly.springapp.exception.DuplicateSnackException;
import com.examly.springapp.exception.InvalidSnackException;
import com.examly.springapp.exception.SnackNotFoundException;
import com.examly.springapp.mapper.VeganSnackMapper;
import com.examly.springapp.model.SnackCategory;
import com.examly.springapp.model.VeganSnack;
import com.examly.springapp.repository.VeganSnackRepo;
import com.examly.springapp.utility.SnackPricingPolicy;
import com.examly.springapp.utility.SnackValidator;

/**
 * Unit tests of the catalogue service.
 *
 * <p>The tests exercise the validation rules, the duplicate detection, the category resolution,
 * the pricing policy and the aggregated catalogue figures against a mocked repository.</p>
 */
@ExtendWith(MockitoExtension.class)
class VeganSnackServiceTests {

    /** Clock fixed to a known point in time so that the derived dates are deterministic. */
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    /** Data access gateway of the catalogue. */
    @Mock
    private VeganSnackRepo veganSnackRepo;

    /** Validator applied to every submitted payload. */
    private SnackValidator snackValidator;

    /** Policy providing the derived pricing and freshness information. */
    private SnackPricingPolicy snackPricingPolicy;

    /** Converter between catalogue records and their transport representation. */
    private VeganSnackMapper veganSnackMapper;

    /** Service under test. */
    private VeganSnackService veganSnackService;

    /**
     * Builds the service under test with the real collaborators and a mocked repository.
     */
    @BeforeEach
    void setUp() {
        snackValidator = new SnackValidator();
        snackPricingPolicy = new SnackPricingPolicy(FIXED_CLOCK);
        veganSnackMapper = new VeganSnackMapper(snackPricingPolicy);
        veganSnackService = new VeganSnackService(veganSnackRepo, snackValidator, snackPricingPolicy,
                veganSnackMapper);
    }

    /**
     * A valid payload is stored and the derived values are returned to the caller.
     */
    @Test
    void addVeganSnack_withValidPayload_storesEntryAndReturnsDerivedValues() {
        when(veganSnackRepo.existsBySnackNameIgnoreCase("Roasted Almonds")).thenReturn(false);
        when(veganSnackRepo.save(any(VeganSnack.class))).thenAnswer(invocation -> {
            VeganSnack entity = invocation.getArgument(0);
            entity.setId(7L);
            entity.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
            return entity;
        });

        VeganSnackResponse response = veganSnackService.addVeganSnack(validRequest());

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getSnackName()).isEqualTo("Roasted Almonds");
        assertThat(response.getSnackType()).isEqualTo("Nuts and Seeds");
        assertThat(response.getCategory()).isEqualTo("NUTS_AND_SEEDS");
        assertThat(response.getQuantity()).isEqualTo(12);
        assertThat(response.getPrice()).isEqualByComparingTo("149.50");
        assertThat(response.getPriceBand()).isEqualTo("STANDARD");
        assertThat(response.getFreshnessStatus()).isEqualTo("FRESH");
        assertThat(response.getTotalValue()).isEqualByComparingTo("1794.00");
        assertThat(response.getExpiryDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(response.getIsVegan()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
    }

    /**
     * The submitted values are trimmed and repeated whitespace is collapsed before they are stored.
     */
    @Test
    void addVeganSnack_trimsAndCollapsesWhitespace() {
        when(veganSnackRepo.existsBySnackNameIgnoreCase("Roasted Almonds")).thenReturn(false);
        when(veganSnackRepo.save(any(VeganSnack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeganSnackRequest request = new VeganSnackRequest("  Roasted   Almonds  ", "  Nuts and   Seeds ",
                12, new BigDecimal("149.50"), 6);
        veganSnackService.addVeganSnack(request);

        ArgumentCaptor<VeganSnack> captor = ArgumentCaptor.forClass(VeganSnack.class);
        verify(veganSnackRepo).save(captor.capture());
        assertThat(captor.getValue().getSnackName()).isEqualTo("Roasted Almonds");
        assertThat(captor.getValue().getSnackType()).isEqualTo("Nuts and Seeds");
    }

    /**
     * An invalid payload is rejected and every faulty field is reported back.
     */
    @Test
    void addVeganSnack_withInvalidPayload_reportsAllFieldErrors() {
        VeganSnackRequest request = new VeganSnackRequest("", "", 0, new BigDecimal("-5"), -1);

        assertThatThrownBy(() -> veganSnackService.addVeganSnack(request))
                .isInstanceOf(InvalidSnackException.class)
                .satisfies(thrown -> {
                    InvalidSnackException exception = (InvalidSnackException) thrown;
                    assertThat(exception.getMessage()).isEqualTo("Snack details are invalid.");
                    assertThat(exception.getFieldErrors())
                            .containsEntry("snackName", "Snack Name is required")
                            .containsEntry("snackType", "Snack Type is required")
                            .containsEntry("quantity", "Quantity must be at least 1")
                            .containsEntry("price", "Price must be a positive number")
                            .containsEntry("expiryInMonths", "Expiry must be a non-negative number");
                });
        verify(veganSnackRepo, never()).save(any(VeganSnack.class));
    }

    /**
     * A payload without the optional fields is rejected with a message per missing field.
     */
    @Test
    void addVeganSnack_withMissingValues_reportsMissingFields() {
        assertThatThrownBy(() -> veganSnackService.addVeganSnack(new VeganSnackRequest()))
                .isInstanceOf(InvalidSnackException.class)
                .satisfies(thrown -> {
                    Map<String, String> fieldErrors = ((InvalidSnackException) thrown).getFieldErrors();
                    assertThat(fieldErrors)
                            .containsEntry("snackName", "Snack Name is required")
                            .containsEntry("snackType", "Snack Type is required")
                            .containsEntry("quantity", "Quantity is required")
                            .containsEntry("price", "Price is required")
                            .containsEntry("expiryInMonths", "Expiry in months is required");
                });
    }

    /**
     * A name that is already used by another entry is rejected as a conflict.
     */
    @Test
    void addVeganSnack_withExistingName_isRejectedAsDuplicate() {
        when(veganSnackRepo.existsBySnackNameIgnoreCase("Roasted Almonds")).thenReturn(true);

        assertThatThrownBy(() -> veganSnackService.addVeganSnack(validRequest()))
                .isInstanceOf(DuplicateSnackException.class)
                .satisfies(thrown -> {
                    DuplicateSnackException exception = (DuplicateSnackException) thrown;
                    assertThat(exception.getFieldErrors())
                            .containsEntry("snackName", "A snack with this name already exists");
                });
        verify(veganSnackRepo, never()).save(any(VeganSnack.class));
    }

    /**
     * The free text type is mapped onto the catalogue taxonomy.
     *
     * @param snackType        free text type description
     * @param expectedCategory expected catalogue category
     */
    @ParameterizedTest
    @CsvSource({
            "Nuts and Seeds, NUTS_AND_SEEDS",
            "Chips, CHIPS_AND_CRUNCHIES",
            "Fruit Snacks, FRUIT_SNACKS",
            "Chocolate, CHOCOLATE",
            "Energy Bars, ENERGY_BARS",
            "Baked Treats, BAKED_TREATS",
            "Granola Mix, OTHERS"
    })
    void addVeganSnack_resolvesCategoryFromSnackType(String snackType, String expectedCategory) {
        when(veganSnackRepo.existsBySnackNameIgnoreCase(anyString())).thenReturn(false);
        when(veganSnackRepo.save(any(VeganSnack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeganSnackRequest request = new VeganSnackRequest("Sample", snackType, 5, new BigDecimal("10.00"), 4);
        VeganSnackResponse response = veganSnackService.addVeganSnack(request);

        assertThat(response.getCategory()).isEqualTo(expectedCategory);
    }

    /**
     * The category resolution ignores the letter case and the surrounding whitespace.
     */
    @Test
    void addVeganSnack_resolvesCategoryCaseInsensitively() {
        when(veganSnackRepo.existsBySnackNameIgnoreCase(anyString())).thenReturn(false);
        when(veganSnackRepo.save(any(VeganSnack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeganSnackRequest request = new VeganSnackRequest("Sample", "  cHoCoLaTe  ", 5, new BigDecimal("10.00"), 4);
        VeganSnackResponse response = veganSnackService.addVeganSnack(request);

        assertThat(response.getCategory()).isEqualTo(SnackCategory.CHOCOLATE.name());
    }

    /**
     * The price band follows the commercial bands of the catalogue.
     *
     * @param price        unit price
     * @param expectedBand expected price band
     */
    @ParameterizedTest
    @CsvSource({
            "99.99, BUDGET",
            "100.00, STANDARD",
            "499.99, STANDARD",
            "500.00, PREMIUM",
            "1999.99, PREMIUM",
            "2000.00, ARTISAN"
    })
    void priceBand_isResolvedFromTheUnitPrice(String price, String expectedBand) {
        assertThat(snackPricingPolicy.priceBand(new BigDecimal(price))).isEqualTo(expectedBand);
    }

    /**
     * The freshness status follows the remaining shelf life.
     *
     * @param expiryInMonths  remaining shelf life in months
     * @param expectedStatus  expected freshness status
     */
    @ParameterizedTest
    @CsvSource({
            "0, SHELF_STABLE",
            "1, EXPIRING_SOON",
            "2, EXPIRING_SOON",
            "3, FRESH",
            "11, FRESH",
            "12, SHELF_STABLE",
            "36, SHELF_STABLE"
    })
    void freshnessStatus_isResolvedFromTheShelfLife(int expiryInMonths, String expectedStatus) {
        assertThat(snackPricingPolicy.freshnessStatus(expiryInMonths).name()).isEqualTo(expectedStatus);
    }

    /**
     * The expiry date is calculated from the current date and the remaining shelf life.
     */
    @Test
    void expiryDate_isCalculatedFromTheCurrentDate() {
        assertThat(snackPricingPolicy.expiryDate(0)).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(snackPricingPolicy.expiryDate(6)).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(snackPricingPolicy.expiryDate(12)).isEqualTo(LocalDate.of(2027, 1, 15));
    }

    /**
     * The inventory value is the unit price multiplied by the quantity and rounded to two decimals.
     */
    @Test
    void inventoryValue_isCalculatedWithTwoDecimals() {
        assertThat(snackPricingPolicy.inventoryValue(new BigDecimal("149.50"), 12)).isEqualByComparingTo("1794.00");
        assertThat(snackPricingPolicy.inventoryValue(new BigDecimal("19.99"), 3)).isEqualByComparingTo("59.97");
        assertThat(snackPricingPolicy.inventoryValue(new BigDecimal("10.00"), 0)).isEqualByComparingTo("0.00");
    }

    /**
     * The catalogue is returned newest first and never as {@code null}.
     */
    @Test
    void getAllVeganSnacks_returnsEntriesNewestFirst() {
        VeganSnack older = catalogueEntry(1L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4);
        VeganSnack newer = catalogueEntry(2L, "Roasted Almonds", SnackCategory.NUTS_AND_SEEDS, 25, "149.50", 6);
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(List.of(newer, older));

        List<VeganSnackResponse> responses = veganSnackService.getAllVeganSnacks();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(2L);
        assertThat(responses.get(1).getId()).isEqualTo(1L);
    }

    /**
     * An empty catalogue is returned as an empty list.
     */
    @Test
    void getAllVeganSnacks_withEmptyCatalogue_returnsEmptyList() {
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(new ArrayList<>());

        assertThat(veganSnackService.getAllVeganSnacks()).isNotNull().isEmpty();
    }

    /**
     * An unknown identifier is reported as a missing entry.
     */
    @Test
    void getSnackById_withUnknownId_isRejected() {
        when(veganSnackRepo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> veganSnackService.getSnackById(99L))
                .isInstanceOf(SnackNotFoundException.class)
                .hasMessage("Vegan snack not found with id 99");
    }

    /**
     * A known identifier is resolved to its catalogue entry.
     */
    @Test
    void getSnackById_withKnownId_returnsEntry() {
        VeganSnack entity = catalogueEntry(3L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4);
        when(veganSnackRepo.findById(3L)).thenReturn(java.util.Optional.of(entity));

        VeganSnackResponse response = veganSnackService.getSnackById(3L);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getSnackName()).isEqualTo("Banana Chips");
        assertThat(response.getCategory()).isEqualTo("CHIPS_AND_CRUNCHIES");
    }

    /**
     * An update of an unknown entry is rejected.
     */
    @Test
    void updateVeganSnack_withUnknownId_isRejected() {
        when(veganSnackRepo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> veganSnackService.updateVeganSnack(99L, validRequest()))
                .isInstanceOf(SnackNotFoundException.class);
        verify(veganSnackRepo, never()).save(any(VeganSnack.class));
    }

    /**
     * An update of a known entry applies the submitted values.
     */
    @Test
    void updateVeganSnack_withKnownId_appliesSubmittedValues() {
        VeganSnack entity = catalogueEntry(4L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4);
        when(veganSnackRepo.findById(4L)).thenReturn(java.util.Optional.of(entity));
        when(veganSnackRepo.existsBySnackNameIgnoreCaseAndIdNot("Dried Mango Slices", 4L)).thenReturn(false);
        when(veganSnackRepo.save(any(VeganSnack.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeganSnackRequest request = new VeganSnackRequest("Dried Mango Slices", "Fruit Snacks", 30,
                new BigDecimal("249.75"), 9);
        VeganSnackResponse response = veganSnackService.updateVeganSnack(4L, request);

        assertThat(response.getSnackName()).isEqualTo("Dried Mango Slices");
        assertThat(response.getCategory()).isEqualTo("FRUIT_SNACKS");
        assertThat(response.getTotalValue()).isEqualByComparingTo("7492.50");
    }

    /**
     * An update that reuses the name of another entry is rejected as a conflict.
     */
    @Test
    void updateVeganSnack_withNameOfAnotherEntry_isRejectedAsDuplicate() {
        VeganSnack entity = catalogueEntry(4L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4);
        when(veganSnackRepo.findById(4L)).thenReturn(java.util.Optional.of(entity));
        when(veganSnackRepo.existsBySnackNameIgnoreCaseAndIdNot("Roasted Almonds", 4L)).thenReturn(true);

        assertThatThrownBy(() -> veganSnackService.updateVeganSnack(4L, validRequest()))
                .isInstanceOf(DuplicateSnackException.class);
        verify(veganSnackRepo, never()).save(any(VeganSnack.class));
    }

    /**
     * A known entry is removed from the catalogue.
     */
    @Test
    void deleteVeganSnack_withKnownId_removesEntry() {
        VeganSnack entity = catalogueEntry(5L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4);
        when(veganSnackRepo.findById(5L)).thenReturn(java.util.Optional.of(entity));

        veganSnackService.deleteVeganSnack(5L);

        verify(veganSnackRepo).delete(entity);
    }

    /**
     * The removal of an unknown entry is rejected.
     */
    @Test
    void deleteVeganSnack_withUnknownId_isRejected() {
        when(veganSnackRepo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> veganSnackService.deleteVeganSnack(99L))
                .isInstanceOf(SnackNotFoundException.class);
        verify(veganSnackRepo, never()).delete(any(VeganSnack.class));
    }

    /**
     * A type filter narrows the catalogue down to a single category.
     */
    @Test
    void search_withTypeFilter_returnsEntriesOfThatCategory() {
        VeganSnack entity = catalogueEntry(1L, "Roasted Almonds", SnackCategory.NUTS_AND_SEEDS, 25, "149.50", 6);
        when(veganSnackRepo.findByCategoryOrderByIdDesc(SnackCategory.NUTS_AND_SEEDS)).thenReturn(List.of(entity));

        List<VeganSnackResponse> responses = veganSnackService.search("Nuts and Seeds", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("NUTS_AND_SEEDS");
    }

    /**
     * A type filter that cannot be mapped onto a category matches the free text type.
     */
    @Test
    void search_withUnmappedTypeFilter_matchesTheFreeTextType() {
        VeganSnack entity = catalogueEntry(1L, "Granola Mix", SnackCategory.OTHERS, 10, "59.00", 5);
        when(veganSnackRepo.findBySnackTypeContainingIgnoreCaseOrderByIdDesc("Granola"))
                .thenReturn(List.of(entity));

        List<VeganSnackResponse> responses = veganSnackService.search("Granola", null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSnackName()).isEqualTo("Granola Mix");
    }

    /**
     * A search term matches the name and the free text type of the entries.
     */
    @Test
    void search_withSearchTerm_matchesNameAndType() {
        VeganSnack entity = catalogueEntry(1L, "Roasted Almonds", SnackCategory.NUTS_AND_SEEDS, 25, "149.50", 6);
        when(veganSnackRepo.findBySnackNameContainingIgnoreCaseOrSnackTypeContainingIgnoreCaseOrderByIdDesc(
                "Almond", "Almond")).thenReturn(List.of(entity));

        List<VeganSnackResponse> responses = veganSnackService.search(null, "Almond");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSnackName()).isEqualTo("Roasted Almonds");
    }

    /**
     * Without a filter the complete catalogue is returned.
     */
    @Test
    void search_withoutFilter_returnsCompleteCatalogue() {
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(new ArrayList<>());

        assertThat(veganSnackService.search(null, null)).isNotNull().isEmpty();
    }

    /**
     * The dashboard figures aggregate the catalogue.
     */
    @Test
    void getCatalogStats_aggregatesTheCatalogue() {
        List<VeganSnack> catalogue = List.of(
                catalogueEntry(1L, "Roasted Almonds", SnackCategory.NUTS_AND_SEEDS, 25, "149.50", 6),
                catalogueEntry(2L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 4),
                catalogueEntry(3L, "Dark Chocolate Bar", SnackCategory.CHOCOLATE, 18, "199.00", 12));
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(catalogue);

        SnackCatalogStatsResponse stats = veganSnackService.getCatalogStats();

        assertThat(stats.getTotalSnacks()).isEqualTo(3L);
        assertThat(stats.getTotalQuantity()).isEqualTo(83L);
        assertThat(stats.getTotalInventoryValue()).isEqualByComparingTo("10519.10");
        assertThat(stats.getAveragePrice()).isEqualByComparingTo("142.83");
        assertThat(stats.getExpiringSoonCount()).isZero();
        assertThat(stats.getExpiredCount()).isZero();
        assertThat(stats.getByCategory()).hasSize(3);
        assertThat(stats.getByCategory().get(0).getCategory()).isEqualTo("CHIPS_AND_CRUNCHIES");
        assertThat(stats.getByCategory().get(0).getCount()).isEqualTo(1L);
        assertThat(stats.getByCategory().get(0).getTotalQuantity()).isEqualTo(40L);
        assertThat(stats.getByCategory().get(0).getTotalValue()).isEqualByComparingTo("3199.60");
        assertThat(stats.getByCategory().get(1).getCategory()).isEqualTo("NUTS_AND_SEEDS");
        assertThat(stats.getByCategory().get(1).getTotalValue()).isEqualByComparingTo("3737.50");
    }

    /**
     * Entries that expire soon are counted separately.
     */
    @Test
    void getCatalogStats_countsExpiringSoonEntries() {
        List<VeganSnack> catalogue = List.of(
                catalogueEntry(1L, "Banana Chips", SnackCategory.CHIPS_AND_CRUNCHIES, 40, "79.99", 2),
                catalogueEntry(2L, "Dark Chocolate Bar", SnackCategory.CHOCOLATE, 18, "199.00", 12));
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(catalogue);

        SnackCatalogStatsResponse stats = veganSnackService.getCatalogStats();

        assertThat(stats.getExpiringSoonCount()).isEqualTo(1L);
        assertThat(stats.getExpiredCount()).isZero();
    }

    /**
     * An empty catalogue produces empty dashboard figures.
     */
    @Test
    void getCatalogStats_withEmptyCatalogue_returnsZeroFigures() {
        when(veganSnackRepo.findAllByOrderByIdDesc()).thenReturn(new ArrayList<>());

        SnackCatalogStatsResponse stats = veganSnackService.getCatalogStats();

        assertThat(stats.getTotalSnacks()).isZero();
        assertThat(stats.getTotalQuantity()).isZero();
        assertThat(stats.getTotalInventoryValue()).isEqualByComparingTo("0.00");
        assertThat(stats.getAveragePrice()).isEqualByComparingTo("0.00");
        assertThat(stats.getByCategory()).isEmpty();
    }

    /**
     * The number of catalogue entries is taken from the repository.
     */
    @Test
    void countSnacks_returnsTheNumberOfEntries() {
        when(veganSnackRepo.count()).thenReturn(6L);

        assertThat(veganSnackService.countSnacks()).isEqualTo(6L);
    }

    /**
     * Builds a valid payload for the catalogue.
     *
     * @return the payload
     */
    private VeganSnackRequest validRequest() {
        return new VeganSnackRequest("Roasted Almonds", "Nuts and Seeds", 12, new BigDecimal("149.50"), 6);
    }

    /**
     * Builds a catalogue entry for the read and aggregation tests.
     *
     * @param id           identifier of the entry
     * @param snackName    name of the product
     * @param category     catalogue category
     * @param quantity     number of units held
     * @param price        price of a single unit
     * @param expiryInMonths remaining shelf life in months
     * @return the catalogue entry
     */
    private VeganSnack catalogueEntry(Long id, String snackName, SnackCategory category, int quantity, String price,
                                      int expiryInMonths) {
        VeganSnack entity = new VeganSnack(snackName, snackName, category, quantity, new BigDecimal(price),
                expiryInMonths);
        entity.setId(id);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        return entity;
    }
}
