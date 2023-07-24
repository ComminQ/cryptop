package net.cryptop.strategy;

import java.util.List;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongList;
import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.data.DataClasses.Trade;
import net.cryptop.data.DataFrame;
import net.cryptop.wallet.Wallet;

/**
 * A strategy.
 */
public abstract class Strategy {

  /**
   * A strategy result.
   * Contains the final wallet and the list of trades.
   * Used to display the result of the strategy.
   * 
   * @param wallet      final wallet
   * @param trades      list of trades
   * @param walletValue wallet value over time
   */
  public record StrategyResult(Wallet wallet, List<Trade> trades,
      DoubleList walletValue) {

    public DataFrame toDataFrame(HistoricalData base) {
      var dataFrame = new DataFrame();

      LongList dates = LongList.of(
          base.candles().stream().mapToLong(c -> c.date()).toArray());
      dataFrame.addField("date", dates);
      dataFrame.addField("value", walletValue);
      // list trades
      var buy = new DoubleArrayList();
      var sells = new DoubleArrayList();
      // for each candle
      Long2ObjectMap<Trade> tradesBuyMap = trades.stream()
          .collect(Long2ObjectOpenHashMap::new,
              (map, trade) -> map.put(trade.start(), trade),
              Long2ObjectOpenHashMap::putAll);

      Long2ObjectMap<Trade> tradesSellMap = trades.stream()
          .collect(Long2ObjectOpenHashMap::new,
              (map, trade) -> map.put(trade.end(), trade),
              Long2ObjectOpenHashMap::putAll);

      for (var candle : base.candles()) {
        // if there is a trade
        var trade = tradesBuyMap.get(candle.date());
        if (trade != null) {
          buy.add(candle.close());
        } else {
          buy.add(Double.NaN);
        }

        trade = tradesSellMap.get(candle.date());
        if (trade != null) {
          sells.add(candle.close());
        } else {
          sells.add(Double.NaN);
        }
      }
      dataFrame.addField("buy", buy);
      dataFrame.addField("sell", sells);

      return dataFrame;
    }
  }

  /**
   * Get the name of the strategy.
   *
   * @return the name of the strategy
   */
  public abstract String getName();

  /**
   * Process of the strategy.
   *
   * @param currentWallet  current wallet
   * @param historicalData historical data
   * @param dataFrame      data frame
   */
  public abstract StrategyResult run(Wallet startingWallet, HistoricalData historicalData,
      DataFrame dataFrame);
}
