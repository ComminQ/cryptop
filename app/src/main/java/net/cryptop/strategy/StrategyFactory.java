package net.cryptop.strategy;

import java.util.Map;

public class StrategyFactory {

  private StrategyFactory() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Create a strategy from its name.
   *
   * @param strategyName strategy name
   * @params params strategy parameters
   *
   * @return a strategy instance with the given parameters
   */
  public static Strategy createStrategy(String strategyName,
                                        Map<String, String> params) {
    return switch (strategyName) {
      case "HOLD" -> new HoldStrategy();
      case "CUSTOM_NOISE1" -> new CustomNoiseV1Strategy(params.get("noiseField"), Double.parseDouble(params.get("lowThreshold")), Double.parseDouble(params.get("highThreshold")));
      default -> throw new IllegalStateException("Unexpected value: " + strategyName);
    };
  }

}
