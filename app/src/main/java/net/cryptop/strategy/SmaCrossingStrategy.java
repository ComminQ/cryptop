package net.cryptop.strategy;

import net.cryptop.data.DataFrame;

public class SmaCrossingStrategy extends BuySellSignalsStrategy {

  private String shortSma;
  private String longSma;

  public SmaCrossingStrategy(String shortSma, String longSma) {
    this.shortSma = shortSma;
    this.longSma = longSma;
  }

  @Override
  public String getName() {
    return "SMA_CROSSING";
  }
  @Override
  public boolean buySignal(DataFrame dataFrame, double currentCrypto,
                           long currenDtate, double currentStableCoin,
                           int index) {
    var shortSmaValue = dataFrame.getDouble(shortSma, index);
    var longSmaValue = dataFrame.getDouble(longSma, index);
    return shortSmaValue > longSmaValue;
  }
  @Override
  public boolean sellSignal(DataFrame dataFrame, double currentCrypto,
                            long currenDtate, double currentStableCoin,
                            int index) {
    var shortSmaValue = dataFrame.getDouble(shortSma, index);
    var longSmaValue = dataFrame.getDouble(longSma, index);
    return shortSmaValue < longSmaValue;
  }
}
