package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AnimaPlant {
    NONE("None"), ATTAS("Attas"), IASOR("Iasor"), KRONOS("Kronos");

    private final String name;

    /**
     * Get the disease chance modifier for this plant. 20% for Iasor, 100% for
     * everything else.
     *
     * @return Disease chance, out of 1
     */
    public double getDiseaseChanceModifier() {
        return this == AnimaPlant.IASOR ? 0.2 : 1.0;
    }

    /**
     * Get the "chance to save" bonus provided by this plant. 5% for Attas, 0
     * for everything else.
     *
     * @see https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
     * @return Chance to save bonus, out of 1
     */
    public double getChanceToSaveBonus() {
        return this == AnimaPlant.ATTAS ? 0.05 : 0.0;
    }
}
