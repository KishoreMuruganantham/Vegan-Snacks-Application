package com.examly.springapp.configuration;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.examly.springapp.dto.request.VeganSnackRequest;
import com.examly.springapp.repository.VeganSnackRepo;
import com.examly.springapp.service.VeganSnackService;

/**
 * Seeds the catalogue with a deterministic set of reference snacks.
 *
 * <p>The seeder only runs when the catalogue is empty, which keeps the application idempotent
 * across restarts and makes a fresh environment immediately usable. The seed data is written
 * through the regular service so that the reference entries obey the same rules as the entries
 * submitted by a user.</p>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    /** Logger used to trace the seeding of the catalogue. */
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    /** Reference entries of the catalogue. */
    private static final List<VeganSnackRequest> SEED_SNACKS = List.of(
            seedRequest("Roasted Almonds", "Nuts and Seeds", 25, "149.50", 6),
            seedRequest("Banana Chips", "Chips", 40, "79.99", 4),
            seedRequest("Dark Chocolate Bar", "Chocolate", 18, "199.00", 12),
            seedRequest("Dried Mango Slices", "Fruit Snacks", 30, "249.75", 9),
            seedRequest("Quinoa Energy Bar", "Energy Bars", 60, "89.00", 8),
            seedRequest("Multigrain Crackers", "Baked Treats", 45, "129.00", 5));

    /** Data access gateway of the catalogue. */
    private final VeganSnackRepo veganSnackRepo;

    /** Core service of the vegan snack catalogue. */
    private final VeganSnackService veganSnackService;

    /**
     * Creates the seeder.
     *
     * @param veganSnackRepo   data access gateway of the catalogue
     * @param veganSnackService core service of the vegan snack catalogue
     */
    public DataSeeder(VeganSnackRepo veganSnackRepo, VeganSnackService veganSnackService) {
        this.veganSnackRepo = veganSnackRepo;
        this.veganSnackService = veganSnackService;
    }

    /**
     * Seeds the catalogue when it is still empty.
     *
     * @param args command line arguments supplied to the application, not used
     */
    @Override
    public void run(String... args) {
        long existingEntries = veganSnackRepo.count();
        if (existingEntries > 0) {
            logger.info("Catalogue already holds {} entries, skipping the seed data", existingEntries);
            return;
        }
        for (VeganSnackRequest seedSnack : SEED_SNACKS) {
            veganSnackService.addVeganSnack(seedSnack);
        }
        logger.info("Seeded the catalogue with {} reference snacks", SEED_SNACKS.size());
    }

    /**
     * Builds a seed entry.
     *
     * @param snackName      name of the product
     * @param snackType      free text type description
     * @param quantity       number of units held in stock
     * @param price          price of a single unit
     * @param expiryInMonths remaining shelf life in months
     * @return the seed entry
     */
    private static VeganSnackRequest seedRequest(String snackName, String snackType, int quantity, String price,
                                                  int expiryInMonths) {
        return new VeganSnackRequest(snackName, snackType, quantity, new BigDecimal(price), expiryInMonths);
    }
}
