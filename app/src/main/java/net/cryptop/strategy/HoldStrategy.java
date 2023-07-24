package net.cryptop.strategy;

import net.cryptop.data.DataFrame;

/**
 * An HOLD strategy.
 *
 * This strategy does nothing.
 * Just hold the initial wallet.
 */
public class HoldStrategy extends BuySellSignalsStrategy {

  @Override
  public String getName() {
    return "HOLD";
  }

  @Override
  public boolean buySignal(DataFrame dataFrame, double currentCrypto, long currenDate, double currentStableCoin,
      int index) {
    return true;
  }

  @Override
  public boolean sellSignal(DataFrame dataFrame, double currentCrypto, long currenDate, double currentStableCoin,
      int index) {
    // check if index is last index in dataFrame
    return index == dataFrame.size() - 1;
  }

}
