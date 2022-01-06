package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public enum Herb {
    GUAM("Guam", 9, 25.0f, 11.0, 12.5, ItemID.GUAM_SEED, ItemID.GRIMY_GUAM_LEAF),
    MARRENTILL("Marrentill", 14, 28.0, 13.5, 15.0, ItemID.MARRENTILL_SEED,
            ItemID.GRIMY_MARRENTILL),
    TARROMIN("Tarromin", 19, 31.0, 16.0, 18.0, ItemID.TARROMIN_SEED, ItemID.GRIMY_TARROMIN),
    HARRALANDER("Harralander", 26, 36.0, 21.5, 24.0, ItemID.HARRALANDER_SEED,
            ItemID.GRIMY_HARRALANDER),
    RANARR("Ranarr", 32, 39.0, 27.0, 30.5, ItemID.RANARR_SEED, ItemID.GRIMY_RANARR_WEED),
    TOADFLAX("Toadflax", 38, 43.0, 34.0, 38.5, ItemID.TOADFLAX_SEED, ItemID.GRIMY_TOADFLAX),
    IRIT("Irit", 44, 46.0, 43.0, 48.5, ItemID.IRIT_SEED, ItemID.GRIMY_IRIT_LEAF),
    AVANTOE("Avantoe", 50, 50.0, 54.5, 61.5, ItemID.AVANTOE_SEED, ItemID.GRIMY_AVANTOE),
    KWUARM("Kwuarm", 56, 54.0, 69.0, 78.0, ItemID.KWUARM_SEED, ItemID.GRIMY_KWUARM),
    SNAPDRAGON("Snapdragon", 62, 57.0, 87.5, 98.5, ItemID.SNAPDRAGON_SEED,
            ItemID.GRIMY_SNAPDRAGON),
    CADANTINE("Cadantine", 67, 60.0, 106.5, 120.0, ItemID.CADANTINE_SEED, ItemID.GRIMY_CADANTINE),
    LANTADYME("Lantadyme", 73, 64.0, 134.5, 151.5, ItemID.LANTADYME_SEED, ItemID.GRIMY_LANTADYME),
    DWARF_WEED("Dwarf Weed", 79, 67.0, 170.5, 192.0, ItemID.DWARF_WEED_SEED,
            ItemID.GRIMY_DWARF_WEED),
    TORSTOL("Torstol", 85, 71.0, 199.5, 224.5, ItemID.TORSTOL_SEED, ItemID.GRIMY_TORSTOL);

    private final String name;
    private final int level;
    /**
     * The *minimum* "chance to save a life" for the herb, at level 1. Values from
     * https://oldschool.runescape.wiki/w/Calculator:Farming/Herbs/Template?action=edit
     */
    private final double minChanceToSave;
    private final double plantXp;
    private final double harvestXp;
    private final int seedItem;
    private final int grimyHerbItem;
}
