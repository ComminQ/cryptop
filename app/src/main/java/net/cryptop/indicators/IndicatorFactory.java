package net.cryptop.indicators;

import java.util.Map;

public final class IndicatorFactory {

  private IndicatorFactory() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Create an indicator from its name and parameters.
   *
   * @param name indicator name
   * @param params indicator parameters
   * @return an indicator instance with the given parameters
   * @throws IllegalStateException if the indicator name is not recognized
   */
  public static Indicator createIndicator(String name,
                                          Map<String, String> params) {
    return switch (name) {
      case "SMA" -> new SMAIndicator(Integer.parseInt(params.get("period")));
      case "EMA" -> new EMAIndicator(Integer.parseInt(params.get("period")));
      case "Savgol" -> new SavgolFilterIndicator(Integer.parseInt(params.get("period")), Integer.parseInt(params.get("polynomial")));
      case "NoiseSavgol" -> new NoiseSavgol();
      case "RSI" -> new RSIIndicator(Integer.parseInt(params.get("period")));
      default -> throw new IllegalStateException("Unexpected value: " + name);
    };
  }

}
