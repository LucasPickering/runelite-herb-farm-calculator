package me.lucaspickering;

import java.util.HashSet;
import java.util.Set;

import me.lucaspickering.utils.AnimaPlant;
import me.lucaspickering.utils.Compost;
import me.lucaspickering.utils.HerbPatch;
import me.lucaspickering.utils.SortingCriteria;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("herbFarmCalculator")
public interface HerbFarmCalculatorConfig extends Config {

  @ConfigItem(keyName = "patches", name = "Patches", description = "Which patches do you farm? Ctrl+click to select multiple")
  default Set<HerbPatch> patches() {
    return new HashSet<>();
  }

  @ConfigItem(keyName = "patches", name = "", description = "")
  void patches(Set<HerbPatch> patches);

  @ConfigItem(keyName = "useFarmingCape", name = "Farming cape", description = "Do you wear a Farming cape while harvesting herbs?")
  default boolean useFarmingCape() {
    return false;
  }

  @ConfigItem(keyName = "useMagicSecateurs", name = "Magic secateurs", description = "Do you equip Magic secateurs while harvesting herbs?")
  default boolean useMagicSecateurs() {
    return false;
  }

  @ConfigItem(keyName = "useBottomlessBucket", name = "Bottomless bucket", description = "Do you use a bottomless bucket?")
  default boolean useBottomlessBucket() {
    return false;
  }

  @ConfigItem(keyName = "useResurrectCrops", name = "Resurrect Crops", description = "Do you use the Resurrect Crops spell on dead herb patches?")
  default boolean useResurrectCrops() {
    return false;
  }

  @ConfigItem(keyName = "compost", name = "Compost", description = "What kind of compost do you use?")
  default Compost compost() {
    return Compost.NONE;
  }

  @ConfigItem(keyName = "animaPlant", name = "Anima Plant", description = "What kind of Anima plant do you have active?")
  default AnimaPlant animaPlant() {
    return AnimaPlant.NONE;
  }

  @ConfigSection(name = "Sorting", description = "Configure the order in which herbs are displayed on the panel.", position = 8)
  String sortingSection = "Sorting";

  @ConfigItem(keyName = "criteria", name = "Criteria", description = "What criteria should the output be ordered by?", section = sortingSection)
  default SortingCriteria criteria() {
    return SortingCriteria.Level;
  }

  @ConfigItem(keyName = "descending", name = "Descending", description = "Sort in descending order? (Highest first/ Z->A)", section = sortingSection)
  default boolean descending() {return false; }

}
