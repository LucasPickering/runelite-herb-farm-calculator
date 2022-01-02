package me.lucaspickering;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AnimaPlant {
    NONE("None"), ATTAS("Attas"), IASOR("Iasor"), KRONOS("Kronos");

    private final String name;
}
