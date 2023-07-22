package net.cryptop.data;

import com.google.gson.JsonArray;
import java.util.List;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.utils.binance.BinanceData.IntervalEnum;

/**
 * Data classes.
 */
public class DataClasses {

  public static final String DATE_FIELD = "date";
  public static final String OPEN_FIELD = "open";
  public static final String HIGH_FIELD = "high";
  public static final String LOW_FIELD = "low";
  public static final String CLOSE_FIELD = "close";
  public static final String VOLUME_FIELD = "volume";

  private DataClasses() { throw new IllegalStateException("Utility class"); }

  /**
   * A candlestick.
   */
  public record Candle(long date, double open, double high, double low,
                       double close, double volume) {
    // result:
    // [
    //   [
    //     1499040000000,      // Kline open time
    //     "0.01634790",       // Open price
    //     "0.80000000",       // High price
    //     "0.01575800",       // Low price
    //     "0.01577100",       // Close price
    //     "148976.11427815",  // Volume
    //     1499644799999,      // Kline Close time
    //     "2434.19055334",    // Quote asset volume
    //     308,                // Number of trades
    //     "1756.87402397",    // Taker buy base asset volume
    //     "28.46694368",      // Taker buy quote asset volume
    //     "0"                 // Unused field, ignore.
    //   ]
    // ]
    public Candle(JsonArray arr) {
      this(arr.get(0).getAsLong(), arr.get(1).getAsDouble(),
           arr.get(2).getAsDouble(), arr.get(3).getAsDouble(),
           arr.get(4).getAsDouble(), arr.get(5).getAsDouble());
    }
  }

  /**
   * Historical data.
   *
   * @param pair crypto pair
   * @param candles candles
   * @param from start time in milliseconds
   * @param interval interval
   * @param to end time in milliseconds
   */
  public record HistoricalData(CryptoPair pair, List<Candle> candles, long from,
                               IntervalEnum interval, long to) {

    public DataFrame toDataFrame() {
      DataFrame df = new DataFrame();

      df.addField(DATE_FIELD,
                  candles.stream().mapToDouble(Candle::date).toArray());
      df.addField(OPEN_FIELD,
                  candles.stream().mapToDouble(Candle::open).toArray());
      df.addField(HIGH_FIELD,
                  candles.stream().mapToDouble(Candle::high).toArray());
      df.addField(LOW_FIELD,
                  candles.stream().mapToDouble(Candle::low).toArray());
      df.addField(CLOSE_FIELD,
                  candles.stream().mapToDouble(Candle::close).toArray());
      df.addField(VOLUME_FIELD,
                  candles.stream().mapToDouble(Candle::volume).toArray());
      return df;
    }

    public int size() { return candles.size(); }
  }
}
