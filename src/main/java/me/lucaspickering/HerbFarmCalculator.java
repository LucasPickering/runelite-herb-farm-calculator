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
        // Multiply by survival chance to account for dead plants
        double expectedYield = this.calcExpectedYield(herb, patch) * survivalChance;

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
     * Calculate the expected number of herbs to be harvested from a patch
     * **assuming it is already fully grown.** I.e. this does *not* take survival
     * chance into account.
     *
     * @return Expected number of herbs yielded on average
     */
    private double calcExpectedYield(Herb herb, HerbPatch patch) {
        // TODO figure out if this math is right, and either explain or fix it
        return this.config.compost().getHarvestLives() / (1.0 - this.calcChanceToSave(herb, patch));
    }

    /**
     * Calculate the chance to "save a life" when picking an herb. This is
     * variable based on the herb, player's farming level, and applicable yield
     * bonuses.
     *
     * @see https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
     * @param patch The patch being harvested
     * @return Odds of saving a live on each individual harvest, out of 1
     */
    private double calcChanceToSave(Herb herb, HerbPatch patch) {
        // TODO add item bonuses
        // TODO add diary bonuses
        // TODO add attas bonus
        int farmingLevel = 96; // TODO pull from game state

        // Yes, these chances really are supposed to be this big, they're really
        // out of 98, not 1
        double chance1 = herb.getMinChanceToSave();
        double chance99 = 80.0;

        // This comes straight from the wiki, it's a lot easier to read in
        // their formatting (link above). The formatted formula doesn't mention
        // anything about the `floor`s though, but it's in the calculator source
        // https://oldschool.runescape.wiki/w/Calculator:Template/Farming/Herbs2?action=edit
        return Math.floor(
                Math.floor(
                        Math.floor((chance1 * (99.0 - farmingLevel) / 98.0) + (chance99 * (farmingLevel - 1.0) / 98.0))
                                + 1.0))
                / 256.0;
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
