package me.lucaspickering;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Compost {
    NONE("None"),
    NORMAL("Compost"),
    SUPERCOMPOST("Supercompost"),
    ULTRACOMPOST("Ultracompost");

    private final String name;
}
