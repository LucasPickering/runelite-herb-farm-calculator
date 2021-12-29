package me.lucaspickering;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("herbFarmCalculator")
public interface HerbFarmCalculatorConfig extends Config {
    @ConfigItem(
            keyName = "useFarmingCape",
            name = "Farming cape",
            description = "Do you wear a Farming cape while harvesting herbs?"
    )
    default boolean useFarmingCape() {
        return false;
    }

    @ConfigItem(
            keyName = "useMagicSecateurs",
            name = "Magic secateurs",
            description = "Do you equip Magic secateurs while harvesting herbs?"
    )
    default boolean useMagicSecateurs() {
        return false;
    }

    @ConfigItem(
            keyName = "useBottomlessBucket",
            name = "Bottomless bucket",
            description = "Do you use a bottomless bucket?"
    )
    default boolean useBottomlessBucket() {
        return false;
    }

    @ConfigItem(
            keyName = "useResurrectCrops",
            name = "Resurrect Crops",
            description = "Do you use the Resurrect Crops spell on dead herb patches?"
    )
    default boolean useResurrectCrops() {
        return false;
    }
}
