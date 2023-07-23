package net.cryptop.strategy;

import net.cryptop.data.DataFrame;

public class CustomNoiseV1Strategy extends BuySellSignalsStrategy {

  private String noiseField;
  private double lowThreshold;
  private double highThreshold;

  public CustomNoiseV1Strategy(String noiseField, double lowThreshold,
                               double highThreshold) {
    this.noiseField = noiseField;
    this.lowThreshold = lowThreshold;
    this.highThreshold = highThreshold;
  }

  @Override
  public String getName() {
    return "CUSTOM_NOISE1";
  }

  @Override
  public boolean buySignal(DataFrame dataFrame, double currentCrypto,
                           long currenDtate, double currentStableCoin,
                           int index) {

    double noiseValue = dataFrame.getDouble(noiseField, index);
    return noiseValue < lowThreshold;
  }

  @Override
  public boolean sellSignal(DataFrame dataFrame, double currentCrypto,
                            long currenDtate, double currentStableCoin,
                            int index) {
    double noiseValue = dataFrame.getDouble(noiseField, index);
    return noiseValue > highThreshold;
  }
}
