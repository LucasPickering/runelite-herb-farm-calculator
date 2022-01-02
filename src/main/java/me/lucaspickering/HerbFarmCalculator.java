package me.lucaspickering;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HerbFarmCalculator {

  private final HerbFarmCalculatorConfig config;

  public HerbFarmCalculator(HerbFarmCalculatorConfig config) {
    this.config = config;
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
    List<HerbCalculatorPatchResult> patches =
        this.config.patches().stream().map(patch -> this.calculatePatch(herb, patch))
            .collect(Collectors.toList());
    return new HerbCalculatorResult(herb, patches);
  }

  /**
   * Run the calculator for a single herb+patch combo
   */
  private HerbCalculatorPatchResult calculatePatch(Herb herb, HerbPatch patch) {
    return new HerbCalculatorPatchResult(herb, patch, 0.0, 0.0, 0.0, 0.0); // TODO
  }
}
