package me.lucaspickering;

import lombok.extern.slf4j.Slf4j;
import me.lucaspickering.utils.Herb;
import me.lucaspickering.utils.HerbCalculatorResult;
import me.lucaspickering.utils.HerbPatchResult;
import me.lucaspickering.utils.HerbResult;
import me.lucaspickering.utils.SurvivalChance;
import me.lucaspickering.utils.Utils;
import me.lucaspickering.utils.HerbPatch;
import me.lucaspickering.utils.HerbPatchBuffs;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        SurvivalChance survivalChance = this.calcSurvivalChance(patch);

        // Multiply by survival chance to account for dead plants
        double expectedYield = this.calcExpectedYield(herb, patch) * survivalChance.getSurvivalChance();

        double baseXp = this.config.compost().getXp()
                // "Plant" XP isn't granted until harvesting the final herb, which
                // means plants that die don't grant *any* XP beyond the compost
                // (and yes I checked that compost XP is granted at the beginning)
                + herb.getPlantXp() * survivalChance.getSurvivalChance()
                + herb.getHarvestXp() * expectedYield;
        double expectedXp = baseXp * (1.0 + patch.getXpBonus());

        // Calculate cost
        // Bottomless bucket doubles compost, so it halves the cost
        double compostCost = this.config.compost().getPrice(this.itemManager)
                * (this.config.useBottomlessBucket() ? 0.5 : 1.0);
        double seedCost = this.itemManager.getItemPrice(herb.getSeedItem());
        // The cost of runes for the Resurrect Crops *only*. We intentionally
        // ignore teleport costs, because it's extremely variable by
        // player/patch, and almost always insignificant.
        double runeCost = this.getResurrectRuneCost() * survivalChance.getResurrectionCastChance();
        double cost = compostCost + seedCost + runeCost;

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
        // We're looking for the expected value of "number of trials until k failures",
        // where k is the number of harvest lives, and prob of failure (Pf) is
        // 1-chance to save. The odds of a single failure is 1/Pf, so k
        // failures is just k/Pf
        // https://math.stackexchange.com/questions/3378034/expected-number-of-coin-flips-to-see-3-heads
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
     * Calculate the chance of a patch growing to adulthood. This takes into
     * account resurrection (when enabled), and will also return the chance of
     * the player having to cast the resurrection spell (whether or not it's
     * successful).
     *
     * @return Container holding the chance of survival, as well as change
     */
    private SurvivalChance calcSurvivalChance(HerbPatchBuffs patch) {
        if (patch.isDiseaseFree()) {
            return new SurvivalChance(1.0, 0.0);
        }

        // Here's some useful theorycrafting I did to understand this problem.
        // Each patch has 3 meaningful growth stages (really it's 4, but it's
        // impossible for the patch to die on the first cycle because it takes
        // two cycles to go alive->diseased->dead), and 4 possible outcomes on
        // each stage.
        // S = survived
        // D = died w/o resurrection attempted
        // R = resurrect successfully
        // F = resurrection attempted and failed
        //
        // We can assign a probability to each of these, using `s`, `d`, `r`,
        // and `f`. We know `s == 1-d` and `r == 1-f` which is useful.
        //
        // First, the possible outcomes *without* resurrection:
        // Good: SSS
        // Bad: SSD, SD, D
        // Here, the chance of survival is just `s^3`.
        //
        // Now when we factor in resurrection, it gets more complicated (keep in
        // mind that resurrection can only be attemped once per crop):
        // Good: SSS, SSR, SRS, RSS
        // Bad: SSF, SF, F, SRD, RSD, RD
        // So the chance of survival is `s^3 + 3s^2r`, where the first term is
        // survival au naturale and the second is the chance of modern medicine
        // saving our herb and allowing it to live a fully and happy life.
        //
        // Something worth noting is that if a player resurrects a plant, they
        // likely won't be harvesting it on that same run, so *technically* we
        // should roll it over into the subsequent run, but in reality that
        // gives the same result as just harvesting it immediately, so not worth
        // trying to model that.
        //
        // TODO This assumes that after resurrection, the plant proceeds to
        // the subsequent stage, and doesn't repeat the stage that it died on.
        // It's not 100% clear based on the wiki whether that actually happens,
        // but it seems logical. Need to do some in-game testing on this.

        // https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
        // Disease chance is always out of 128 and rounded *down* to the nearest
        // 1/128, with a minimum chance of 1/128
        double baseChance = this.config.compost().getBaseDiseaseChance();
        double modifier = this.config.animaPlant().getDiseaseChanceModifier();
        double numerator = Math.max(Math.floor(baseChance * modifier), 1.0);
        double diseaseChancePerCycle = numerator / 128.0;
        double survivalChancePerCycle = 1.0 - diseaseChancePerCycle;

        // The chance of the plant surviving to adulthood on its own
        double naturalSurvivalChance = Math.pow(survivalChancePerCycle, 3.0);

        if (this.config.useResurrectCrops()) {
            // The chance that the patch (1) gets diseased, (2) is successfully
            // resurrected, and (3) successfully grows to adulthood. See the
            // essay above for why this makes sense.
            double resurrectToAdulthoodChance = 3.0 * Math.pow(survivalChancePerCycle, 2.0) * diseaseChancePerCycle
                    * this.getResurrectionChance();

            // Odds of having to cast Resurrect is just the inverse of the odds
            // surviving on its own
            double resurrectCastChance = 1.0 - naturalSurvivalChance;
            return new SurvivalChance(naturalSurvivalChance + resurrectToAdulthoodChance, resurrectCastChance);
        } else {
            return new SurvivalChance(naturalSurvivalChance, 0.0);
        }
    }

    /**
     * Get the cost of casting Resurrect Crops (assuming no staffs, since the
     * benefit of those is negligible).
     *
     * @see https://oldschool.runescape.wiki/w/Resurrect_Crops
     * @return Cost of all runes to cast Resurrect Crops
     */
    private int getResurrectRuneCost() {
        // 8 souls, 12 nats, 8 bloods, 25 earths
        return this.itemManager.getItemPrice(ItemID.SOUL_RUNE) * 8 +
                this.itemManager.getItemPrice(ItemID.NATURE_RUNE) * 12 +
                this.itemManager.getItemPrice(ItemID.BLOOD_RUNE) * 8 +
                this.itemManager.getItemPrice(ItemID.EARTH_RUNE) * 25;
    }

    /**
     * Calculate the chance of the Resurrect Crops spell succeeding, based on
     * the player's magic level. Scales from 50% at level 78 to 75% at 99.
     *
     * @see https://oldschool.runescape.wiki/w/Resurrect_Crops
     * @return Chance of resurrection succeeding, out of 1
     */
    private double getResurrectionChance() {
        int magicLevel = this.client.getRealSkillLevel(Skill.MAGIC);
        int minLevel = MagicAction.RESURRECT_CROPS.getLevel();

        if (magicLevel < minLevel) {
            // Get outta here kid
            return 0.0;
        }

        // Map the value from the range [78,99] to [0.5,0.75] linearly
        return Utils.mapToRange(magicLevel, minLevel, 99, 0.5, 0.75);
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
