package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A container for an herb patch, paired with its *patch-specific* buffs, e.g.
 * not including yield buffs from magic secateurs or other items.
 */
@AllArgsConstructor
@Getter
public class HerbPatchBuffs {
    private final HerbPatch patch;
    private boolean isDiseaseFree;
    private final double yieldBonus;
    private final double xpBonus;
}
