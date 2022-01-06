package me.lucaspickering.utils;

import java.util.List;
import lombok.Getter;

/**
 * A container class for calculator output. This encapsulates results for *a
 * single herb* but *all patches*.
 */
public class HerbResult {

  @Getter
  private final Herb herb;
  private final List<HerbPatchResult> patches;

  public HerbResult(Herb herb, List<HerbPatchResult> patches) {
    this.herb = herb;
    this.patches = patches;
  }

  /**
   * Get the *average* survival rate across all patches.
   *
   * @return Average of all patches' survival rate
   */
  public double getSurvivalChance() {
    return patches.stream().mapToDouble(HerbPatchResult::getSurvivalChance).average().orElse(0.0);
  }

  /**
   * Get the expected yield across all patches in this result
   *
   * @return Sum of each patch's expected yield
   */
  public double getExpectedYield() {
    return patches.stream().mapToDouble(HerbPatchResult::getExpectedYield).sum();
  }

  /**
   * Get the expected XP gained across all patches in this result.
   *
   * @return Sum of each patch's expected XP
   */
  public double getExpectedXp() {
    return patches.stream().mapToDouble(HerbPatchResult::getExpectedXp).sum();
  }

  /**
   * Get the expected profit across all patches in this result.
   *
   * @return Sum of each patch's profit
   */
  public double getProfit() {
    return patches.stream().mapToDouble(HerbPatchResult::getProfit).sum();
  }

  @Override
  public String toString() {
    return String.format("%f survival / %f yield / %f XP", this.getSurvivalChance() * 100.0, this.getExpectedYield(),
        this.getExpectedXp());
  }
}
