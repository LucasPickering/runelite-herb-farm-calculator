package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A container class for calculator output. This encapsulates results for *a
 * single herb* and *a single patch*.
 */
@AllArgsConstructor
@Getter
public class HerbPatchResult {

  private final Herb herb;
  private final HerbPatch patch;
  private final double survivalChance;
  private final double expectedYield;
  private final double expectedXp;
  private final double cost;
  private final double revenue;

  public double getProfit() {
    return this.revenue - this.cost;
  }
}
