package com.examly.springapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examly.springapp.model.SnackCategory;
import com.examly.springapp.model.VeganSnack;

/**
 * Data access contract for the vegan snack catalogue.
 */
public interface VeganSnackRepo extends JpaRepository<VeganSnack, Long> {

    /**
     * Checks whether a snack with the given name already exists, ignoring letter case.
     *
     * @param snackName name to look for
     * @return {@code true} when a catalogue entry with that name exists
     */
    boolean existsBySnackNameIgnoreCase(String snackName);

    /**
     * Checks whether a snack with the given name exists under a different identifier, ignoring
     * letter case.
     *
     * @param snackName name to look for
     * @param id        identifier of the record that is being updated
     * @return {@code true} when another catalogue entry uses that name
     */
    boolean existsBySnackNameIgnoreCaseAndIdNot(String snackName, Long id);

    /**
     * Returns all catalogue entries, newest record first.
     *
     * @return the catalogue ordered by descending identifier
     */
    List<VeganSnack> findAllByOrderByIdDesc();

    /**
     * Returns all catalogue entries of a single category, newest record first.
     *
     * @param category category to filter on
     * @return the matching catalogue entries
     */
    List<VeganSnack> findByCategoryOrderByIdDesc(SnackCategory category);

    /**
     * Returns all catalogue entries whose free text type contains the given fragment.
     *
     * @param snackType fragment to match, matched case insensitively
     * @return the matching catalogue entries ordered by descending identifier
     */
    List<VeganSnack> findBySnackTypeContainingIgnoreCaseOrderByIdDesc(String snackType);

    /**
     * Returns all catalogue entries whose name or free text type contains the given fragment.
     *
     * @param snackName fragment to match against the name
     * @param snackType fragment to match against the type
     * @return the matching catalogue entries ordered by descending identifier
     */
    List<VeganSnack> findBySnackNameContainingIgnoreCaseOrSnackTypeContainingIgnoreCaseOrderByIdDesc(
            String snackName, String snackType);
}
