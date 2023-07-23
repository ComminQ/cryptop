package net.cryptop.strategy;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.ArrayList;
import java.util.List;
import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.data.DataClasses.Trade;
import net.cryptop.data.DataFrame;
import net.cryptop.wallet.Wallet;

public class CustomNoiseV1Strategy implements Strategy {

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
  public StrategyResult run(Wallet startingWallet,
                            HistoricalData historicalData,
                            DataFrame dataFrame) {
    // for each day
    // if noise is below low threshold, buy
    // if noise is above high threshold, sell
    // else do nothing
    var cryptoPair = historicalData.pair();
    var candles = historicalData.candles();
    List<Trade> trades = new ArrayList<>();
    Trade openTrade = null;
    var walletValues = new DoubleArrayList();

    for (int i = 0; i < candles.size(); i++) {
      var candle = candles.get(i);
      // current wallet state
      var currentCrypto = startingWallet.getBalance(cryptoPair.crypto());
      var currentDate = candle.date();
      var currentStableCoin =
          startingWallet.getBalance(cryptoPair.stableCoin());
      double noiseValue = dataFrame.getDouble(noiseField, i);

      if (openTrade == null) {
        // no open trade
        if (noiseValue < lowThreshold) {
          // buy
          double amount = currentStableCoin / candle.close();
          openTrade = new Trade(amount, currentDate);
          // update wallet
          startingWallet.setBalance(cryptoPair.crypto(),
                                    currentCrypto + amount);
          startingWallet.setBalance(cryptoPair.stableCoin(), 0.0);
        }
      } else {
        // open trade
        if (noiseValue > highThreshold) {
          // sell
          double amount = currentCrypto;
          openTrade = new Trade(openTrade.enter(), amount, openTrade.start(),
                                currentDate);
          // update wallet
          startingWallet.setBalance(cryptoPair.crypto(), 0.0);
          startingWallet.setBalance(cryptoPair.stableCoin(),
                                    currentStableCoin +
                                        amount * candle.close());
          // add trade to trade list
          trades.add(openTrade);
          // close trade
          openTrade = null;
        }
      }

      // get wallet value
      double walletValue =
          startingWallet.getBalance(cryptoPair.stableCoin()) +
          startingWallet.getBalance(cryptoPair.crypto()) * candle.close();
      walletValues.add(walletValue);
    }

    return new StrategyResult(startingWallet, trades, walletValues);
  }
}
