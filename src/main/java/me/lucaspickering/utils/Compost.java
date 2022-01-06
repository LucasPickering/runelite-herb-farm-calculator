package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;

@AllArgsConstructor
@Getter
public enum Compost {
    NONE("None", 0.0, -1, 27.0),
    NORMAL("Compost", 18.0, ItemID.COMPOST, 14.0),
    SUPERCOMPOST("Supercompost", 26.0, ItemID.SUPERCOMPOST, 6.0),
    ULTRACOMPOST("Ultracompost", 36.0, ItemID.ULTRACOMPOST, 3.0);

    private final String name;
    /**
     * The amount of XP gained for spreading this compost.
     */
    private final double xp;
    /**
     * ID of the item associated with this compost (-1 for NONE)
     */
    private final int item;
    private final double baseDiseaseChance;

    /**
     * Get the GE price of this compost item (if any)
     */
    public int getPrice(ItemManager itemManager) {
        if (this.item >= 0) {
            return itemManager.getItemPrice(this.item);
        } else {
            return 0;
        }
    }

    /**
     * Get the number of "harvest lives" that a patch starts with when this
     * compost is applied.
     *
     * @see https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
     * @return Number of starting lives (default is 3)
     */
    public int getHarvestLives() {
        switch (this) {
            case NONE:
                return 3;
            case NORMAL:
                return 4;
            case SUPERCOMPOST:
                return 5;
            case ULTRACOMPOST:
                return 6;
            default:
                return 3;
        }
    }
}
