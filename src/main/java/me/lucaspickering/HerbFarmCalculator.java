package me.lucaspickering;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.ItemManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HerbFarmCalculator {

    private final HerbFarmCalculatorConfig config;
    private final ItemManager itemManager;

    public HerbFarmCalculator(HerbFarmCalculatorConfig config, ItemManager itemManager) {
        this.config = config;
        this.itemManager = itemManager;
    }

    /**
     * Run the calculator for every herb and return all the results in a nice list.
     *
     * @return The results, in a nice list
     */
    public List<HerbCalculatorResult> calculate() {
        log.debug("Running herb calculator");
        return Arrays.stream(Herb.values()).map(this::calculateHerb).collect(Collectors.toList());
    }

    /**
     * Run the calculator for a single herb
     */
    private HerbCalculatorResult calculateHerb(Herb herb) {
        List<HerbCalculatorPatchResult> patches = this.config.patches().stream()
                .map(patch -> this.calculatePatch(herb, patch))
                .collect(Collectors.toList());
        return new HerbCalculatorResult(herb, patches);
    }

    /**
     * Run the calculator for a single herb+patch combo
     */
    private HerbCalculatorPatchResult calculatePatch(Herb herb, HerbPatch patch) {
        double survivalChance = this.calcSurvivalChance(patch);
        log.debug("Survival chance", survivalChance);
        // Multiply by survival chance to account for dead plants
        double expectedYield = patch.calcExpectedYield() * survivalChance;

        double expectedXp = this.config.compost().getXp()
                // "Plant" XP isn't granted until harvesting the final herb, which
                // means plants that die don't grant *any* XP beyond the compost
                // TODO check if compost XP is also only applied on final harvest
                + herb.getPlantXp() * survivalChance
                + herb.getHarvestXp() * expectedYield;

        double cost = this.config.compost().getPrice(this.itemManager)
                + this.itemManager.getItemPrice(herb.getSeedItem());
        double revenue = this.itemManager.getItemPrice(herb.getGrimyHerbItem()) * expectedYield;

        return new HerbCalculatorPatchResult(herb, patch, expectedYield, expectedXp, cost, revenue);
    }

    /**
     * Calculate the chance of a patch growing to adulthood.
     *
     * @return The chance of survival, out of 1
     */
    private double calcSurvivalChance(HerbPatch patch) {
        if (patch.isDiseaseFree()) {
            return 1.0;
        }

        // https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
        // Disease chance is always out of 128 and rounded *down* to the nearest
        // 1/128, with a minimum chance of 1/128
        // TODO add iasor modifier
        double numerator = Math.max(Math.floor(this.config.compost().getBaseDiseaseChance()), 1.0);
        double diseaseChancePerCycle = numerator / 128.0;

        // All herbs have 4 growth cycles, and we want to find the chance of
        // exactly 0 disease instances in 3 (n-1) trials. We use n-1 because
        // the last growth cycle can't have disease
        // https://oldschool.runescape.wiki/w/Seeds#Herb_seeds
        return Utils.binomial(diseaseChancePerCycle, 3, 0);
    }
}
