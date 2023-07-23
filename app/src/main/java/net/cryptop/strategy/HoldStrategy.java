package net.cryptop.strategy;

import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.wallet.Wallet;

/**
 * An HOLD strategy.
 *
 * This strategy does nothing.
 * Just hold the initial wallet.
 */
public class HoldStrategy implements Strategy {

  @Override
  public Wallet run(Wallet startingWallet, HistoricalData historicalData) {
    return startingWallet;
  }

  @Override
  public String getName() {
    return "HOLD";
  }
}
