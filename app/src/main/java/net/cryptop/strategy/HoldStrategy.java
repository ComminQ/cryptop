package net.cryptop.strategy;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.List;
import net.cryptop.data.DataClasses;
import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.data.DataFrame;
import net.cryptop.wallet.Wallet;

/**
 * An HOLD strategy.
 *
 * This strategy does nothing.
 * Just hold the initial wallet.
 */
public class HoldStrategy implements Strategy {

  @Override
  public String getName() {
    return "HOLD";
  }

  @Override
  public StrategyResult run(Wallet startingWallet,
                            HistoricalData historicalData,
                            DataFrame dataFrame) {
    var cryptoPair = historicalData.pair();
    var initialPrice = historicalData.candles().get(0).open();

    // convert all of the stablecoins to crypto pair (imitate the user's wallet)
    var currentCrypto = startingWallet.getBalance(cryptoPair.crypto());
    var currentStableCoin = startingWallet.getBalance(cryptoPair.stableCoin());

    // sell all stable coin in exchange for crypto
    currentCrypto += currentStableCoin / initialPrice;
    startingWallet.setBalance(cryptoPair.crypto(), currentCrypto);
    startingWallet.setBalance(cryptoPair.stableCoin(), 0.0);

    var initialDate = historicalData.candles().get(0).date();
    var finalPrice =
        historicalData.candles().get(historicalData.size() - 1).close();
    var finalDate =
        historicalData.candles().get(historicalData.size() - 1).date();
    var trade =
        new DataClasses.Trade(initialPrice, finalPrice, initialDate, finalDate);

    var list = new DoubleArrayList();
    for (var candle : historicalData.candles()) {
      list.add(candle.close()*currentCrypto);
    }

    var finalWallet = new Wallet();
    // sell all crypto in exchange for stable coin
    finalWallet.setBalance(cryptoPair.crypto(), 0.0);
    finalWallet.setBalance(cryptoPair.stableCoin(), currentCrypto * finalPrice);
    return new StrategyResult(finalWallet, List.of(trade), list);
  }
}
