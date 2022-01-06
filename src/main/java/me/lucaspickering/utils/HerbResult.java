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
}
