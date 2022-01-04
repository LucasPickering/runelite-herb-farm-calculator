package me.lucaspickering;

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
}
