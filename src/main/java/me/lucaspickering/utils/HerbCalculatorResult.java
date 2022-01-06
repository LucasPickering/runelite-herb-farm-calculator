package me.lucaspickering.utils;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A container class that holds the entire output of the calculator, including
 * intermediate values that were derived, which might be useful to the user.
 */
@AllArgsConstructor
@Getter
public class HerbCalculatorResult {
    /**
     * Player's farming level
     */
    private final int farmingLevel;
    /**
     * The patches that are being grown. This contains static information about
     * each patch.
     */
    private final List<HerbPatchBuffs> patches;
    /**
     * Results for each herb, aggregated across all patches. This is the real
     * data that the user cares about.
     */
    private final List<HerbResult> herbs;
}
