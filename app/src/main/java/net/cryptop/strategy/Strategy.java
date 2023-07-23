package net.cryptop.strategy;

import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.wallet.Wallet;

/**
 * A strategy.
 */
public interface Strategy {

  /**
   * Get the name of the strategy.
   *
   * @return the name of the strategy
   */
  String getName();

  /**
   * Run the strategy.
   *
   * @param startingWallet the starting wallet
   * @param historicalData the historical data
   *
   * @return the resulting wallet
   */
  Wallet run(Wallet startingWallet, HistoricalData historicalData);
}
