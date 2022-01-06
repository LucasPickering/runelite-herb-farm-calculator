package me.lucaspickering;

import lombok.extern.slf4j.Slf4j;
import me.lucaspickering.utils.Herb;
import me.lucaspickering.utils.HerbCalculatorResult;
import me.lucaspickering.utils.HerbPatchResult;
import me.lucaspickering.utils.HerbResult;
import me.lucaspickering.utils.HerbPatch;
import me.lucaspickering.utils.HerbPatchBuffs;
import me.lucaspickering.utils.Utils;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// TODO figure out injection on this

@Slf4j
public class HerbFarmCalculator {

    private final Client client;
    private final HerbFarmCalculatorConfig config;
    private final ItemManager itemManager;

    public HerbFarmCalculator(Client client, HerbFarmCalculatorConfig config, ItemManager itemManager) {
        this.client = client;
        this.config = config;
        this.itemManager = itemManager;
    }

    /**
     * Run the calculator for every herb and return all the results in a nice list.
     *
     * @return The results, in a nice list
     */
    public HerbCalculatorResult calculate() {
        log.debug("Running herb calculator");

        // For each patch, figure out its patch-specific buffs. We do this up
        // front so we can render the buffs to the user clearly, separate from
        // the herb outputs
        List<HerbPatchBuffs> patches = this.config.patches().stream()
                .map(patch -> new HerbPatchBuffs(patch, patch.isDiseaseFree(this.client),
                        this.getDiaryChanceToSaveBonus(patch), this.getXpBonus(patch)))
                // Sort alphabetically, for easier reading on the UI
                .sorted(Comparator.comparing(patch -> patch.getPatch().getName()))
                .collect(Collectors.toList());

        List<HerbResult> herbs = Arrays.stream(Herb.values()).map(herb -> this.calculateHerb(herb, patches))
                .collect(Collectors.toList());
        return new HerbCalculatorResult(this.getFarmingLevel(), patches, herbs);
    }

    /**
     * Run the calculator for a single herb
     */
    private HerbResult calculateHerb(Herb herb, List<HerbPatchBuffs> patches) {
        List<HerbPatchResult> patchResults = patches.stream()
                .map(patch -> this.calculatePatch(herb, patch))
                .collect(Collectors.toList());
        return new HerbResult(herb, patchResults);
    }

    /**
     * Run the calculator for a single herb+patch combo
     */
    private HerbPatchResult calculatePatch(Herb herb, HerbPatchBuffs patch) {
        double survivalChance = this.calcSurvivalChance(patch);
        // Multiply by survival chance to account for dead plants
        double expectedYield = this.calcExpectedYield(herb, patch) * survivalChance;

        double baseXp = this.config.compost().getXp()
                // "Plant" XP isn't granted until harvesting the final herb, which
                // means plants that die don't grant *any* XP beyond the compost
                // (and yes I checked that compost XP is granted at the beginning)
                + herb.getPlantXp() * survivalChance
                + herb.getHarvestXp() * expectedYield;
        double expectedXp = baseXp * (1.0 + patch.getXpBonus());

        // Calculate cost
        // Bottomless bucket doubles compost, so it halves the cost
        double compostCost = this.config.compost().getPrice(this.itemManager)
                * (this.config.useBottomlessBucket() ? 0.5 : 1.0);
        double seedCost = this.itemManager.getItemPrice(herb.getSeedItem());
        // We intentionally ignore teleport costs, because it's extremely
        // variable by player/patch, and almost always insignificant
        double cost = compostCost + seedCost;

        // Calculate revenue
        double revenue = this.itemManager.getItemPrice(herb.getGrimyHerbItem()) * expectedYield;

        return new HerbPatchResult(herb, patch.getPatch(), expectedYield, expectedXp, cost, revenue);
    }

    /**
     * Calculate the expected number of herbs to be harvested from a patch
     * **assuming it is already fully grown.** I.e. this does *not* take survival
     * chance into account.
     *
     * @return Expected number of herbs yielded on average
     */
    private double calcExpectedYield(Herb herb, HerbPatchBuffs patch) {
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
    private double calcChanceToSave(Herb herb, HerbPatchBuffs patch) {
        int farmingLevel = this.getFarmingLevel();
        double itemBonus = this.getItemChanceToSaveBonus();
        double attasBonus = this.config.animaPlant().getChanceToSaveBonus();

        // Yes, these chances really are supposed to be this big, they're really
        // out of 98, not 1
        double chance1 = herb.getMinChanceToSave(); // Min chance varies by herb
        double chance99 = 80.0; // Max chance is 80, for all herbs

        // This comes straight from the wiki, it's a lot easier to read in
        // their formatting (link above). The formatted formula doesn't mention
        // anything about the `floor`s though, but it's in the calculator source
        // https://oldschool.runescape.wiki/w/Calculator:Template/Farming/Herbs2?action=edit
        return Math.floor(
                Math.floor(
                        Math.floor((chance1 * (99.0 - farmingLevel) / 98.0) + (chance99 * (farmingLevel - 1.0) / 98.0))
                                * (1.0 + itemBonus)
                                // https://twitter.com/JagexAsh/status/956892754096869376
                                * (1.0 + patch.getYieldBonus())
                                // https://twitter.com/JagexAsh/status/1245644766328446976
                                // Note: This conflicts with how the wiki calculator does it,
                                // but I'm going off of Ash's tweets instead
                                * (1.0 + attasBonus)
                                + 1.0))
                / 256.0;
    }

    /**
     * Calculate the chance of a patch growing to adulthood.
     *
     * @return The chance of survival, out of 1
     */
    private double calcSurvivalChance(HerbPatchBuffs patch) {
        if (patch.isDiseaseFree()) {
            return 1.0;
        }

        // https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
        // Disease chance is always out of 128 and rounded *down* to the nearest
        // 1/128, with a minimum chance of 1/128
        double baseChance = this.config.compost().getBaseDiseaseChance();
        double modifier = this.config.animaPlant().getDiseaseChanceModifier();
        double numerator = Math.max(Math.floor(baseChance * modifier), 1.0);
        double diseaseChancePerCycle = numerator / 128.0;

        // All herbs have 4 growth cycles, and we want to find the chance of
        // exactly 0 disease instances in 3 (n-1) trials. We use n-1 because
        // the last growth cycle can't have disease
        // https://oldschool.runescape.wiki/w/Seeds#Herb_seeds
        return Utils.binomial(diseaseChancePerCycle, 3, 0);
    }

    /**
     * Get the player's farming level
     *
     * @return Farming level, or 0 if not logged in
     */
    private int getFarmingLevel() {
        return this.client.getRealSkillLevel(Skill.FARMING);
    }

    /**
     * Get the "chance to save" bonus due to equipped items.
     *
     * @see https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
     * @return Chance to save bonus, out of 1
     */
    private double getItemChanceToSaveBonus() {
        double bonus = 0.0;
        if (this.config.useMagicSecateurs()) {
            // https://oldschool.runescape.wiki/w/Magic_secateurs
            bonus += 0.1;
        }
        if (this.config.useFarmingCape()) {
            // https://oldschool.runescape.wiki/w/Farming_cape
            bonus += 0.05;
        }
        return bonus;
    }

    /**
     * Get the "chance to save" bonus due to achievement diary bonuses. These
     * bonuses are patch-specific.
     *
     * @see https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
     * @return Chance to save bonus, out of 1
     */
    private double getDiaryChanceToSaveBonus(HerbPatch patch) {
        switch (patch) {
            case CATHERBY:
                // +5% from medium, +10% from hard, +15% from elite
                // https://oldschool.runescape.wiki/w/Kandarin_Diary
                if (this.client.getVar(Varbits.DIARY_KANDARIN_ELITE) > 0) {
                    return 0.15;
                }
                if (this.client.getVar(Varbits.DIARY_KANDARIN_HARD) > 0) {
                    return 0.10;
                }
                if (this.client.getVar(Varbits.DIARY_KANDARIN_MEDIUM) > 0) {
                    return 0.05;
                }
                return 0.0;
            // +5% from Kourend hard
            // https://oldschool.runescape.wiki/w/Kourend_%26_Kebos_Diary#Rewards_3
            case FARMING_GUILD:
            case HOSIDIUS:
                if (this.client.getVar(Varbits.DIARY_KOUREND_HARD) > 0) {
                    return 0.05;
                }
                return 0.0;
            default:
                // Everyone else sucks, and should feel bad
                return 0.0;
        }
    }

    /**
     * Get the XP bonus factor to apply to this patch, from achievement diary
     * rewards.
     *
     * @param patch Herb patch being farmed
     * @return 0 for no bonus, positive number for a bonus
     */
    private double getXpBonus(HerbPatch patch) {
        switch (patch) {
            case FALADOR:
                // +10% from medium
                if (this.client.getVar(Varbits.DIARY_FALADOR_MEDIUM) > 0) {
                    return 0.10;
                }
                return 0.0;
            default:
                return 0.0;
        }
    }
}
