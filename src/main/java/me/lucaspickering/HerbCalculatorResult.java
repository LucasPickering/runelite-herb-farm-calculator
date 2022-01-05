package me.lucaspickering;

import java.util.List;
import lombok.Getter;

/**
 * A container class for calculator output. This encapsulates results for *a
 * single herb* but *all
 * patches*.
 */
public class HerbCalculatorResult {

  @Getter
  private final Herb herb;
  private final List<HerbCalculatorPatchResult> patches;

  public HerbCalculatorResult(Herb herb, List<HerbCalculatorPatchResult> patches) {
    this.herb = herb;
    this.patches = patches;
  }

  /**
   * Get the expected yield across all patches in this result
   *
   * @return Sum of each patch's expected yield
   */
  public double getExpectedYield() {
    return patches.stream().mapToDouble(HerbCalculatorPatchResult::getExpectedYield).sum();
  }

  /**
   * Get the expected XP gained across all patches in this result.
   *
   * @return Sum of each patch's expected XP
   */
  public double getExpectedXp() {
    return patches.stream().mapToDouble(HerbCalculatorPatchResult::getExpectedXp).sum();
  }

  /**
   * Get the expected profit across all patches in this result.
   *
   * @return Sum of each patch's profit
   */
  public double getProfit() {
    return patches.stream().mapToDouble(HerbCalculatorPatchResult::getProfit).sum();
  }
}
