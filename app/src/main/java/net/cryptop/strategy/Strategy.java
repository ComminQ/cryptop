package net.cryptop.strategy;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.List;
import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.data.DataClasses.Trade;
import net.cryptop.data.DataFrame;
import net.cryptop.wallet.Wallet;

/**
 * A strategy.
 */
public interface Strategy {

  /**
   * A strategy result.
   * Contains the final wallet and the list of trades.
   * Used to display the result of the strategy.
   * @param wallet final wallet
   * @param trades list of trades
   * @param walletValue wallet value over time
   */
  record StrategyResult(Wallet wallet, List<Trade> trades,
                        DoubleList walletValue) {

    public DataFrame toDataFrame(HistoricalData base) {
      var dataFrame = new DataFrame();

      LongList dates = LongList.of(
          base.candles().stream().mapToLong(c -> c.date()).toArray());
      dataFrame.addField("date", dates);
      dataFrame.addField("value", walletValue);
      // list trades
      var tradeValues = new DoubleArrayList();
      // for each candle
      for (var candle : base.candles()) {
        // if there is a trade
        var trade =
            trades.stream()
                .filter(
                    t -> t.start() == candle.date() || t.end() == candle.date())
                .findFirst();

        if (trade.isPresent()) {
          // if trade = buy
          if (trade.get().start() == candle.date()) {
            tradeValues.add(1);
          } else {
            // else trade = sell
            tradeValues.add(-1);
          }
        } else {
          // else add 0
          tradeValues.add(Double.NaN);
        }
      }
      dataFrame.addField("trade", tradeValues);

      return dataFrame;
    }
  }

  /**
   * Get the name of the strategy.
   *
   * @return the name of the strategy
   */
  String getName();

  /**
   * Process of the strategy.
   *
   * @param currentWallet current wallet
   * @param historicalData historical data
   * @param dataFrame data frame
   */
  StrategyResult run(Wallet startingWallet, HistoricalData historicalData,
                     DataFrame dataFrame);
}
