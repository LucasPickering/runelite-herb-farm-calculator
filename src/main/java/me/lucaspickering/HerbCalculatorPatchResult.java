package me.lucaspickering;

import lombok.Getter;

/**
 * A container class for calculator output. This encapsulates results for *a
 * single herb* and *a single patch*.
 */
public class HerbCalculatorPatchResult {

  @Getter
  private final Herb herb;
  @Getter
  private final HerbPatch patch;
  @Getter
  private final double expectedYield;
  @Getter
  private final double expectedXp;
  private final double cost;
  private final double revenue;

  public HerbCalculatorPatchResult(Herb herb, HerbPatch patch,
      double expectedYield, double expectedXp, double cost, double revenue) {
    this.herb = herb;
    this.patch = patch;
    this.expectedYield = expectedYield;
    this.expectedXp = expectedXp;
    this.cost = cost;
    this.revenue = revenue;
  }

  public double getProfit() {
    return this.revenue - this.cost;
  }
}
