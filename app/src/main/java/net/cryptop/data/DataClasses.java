package net.cryptop.data;

import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.utils.binance.BinanceData.IntervalEnum;
import net.cryptop.wallet.Wallet;

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

  public Set<String> STABLE_COINS = Set.of("USDT", "BUSD", "USDC", "TUSD");
  public Set<String> CRYPTO_CURRENCIES = Set.of(
      "BTC", "ETH", "BNB", "XRP", "BCH", "LTC", "EOS", "XLM", "TRX", "ADA",
      "XMR", "DASH", "ETC", "NEO", "ZEC", "QTUM", "ZRX", "LINK", "BAT", "IOTA",
      "IOST", "NANO", "ONT", "VET", "THETA", "REN", "RVN", "ALGO", "ZIL", "KNC",
      "ATOM", "TFUEL", "ONE", "FTM", "ENJ", "TOMO", "BAND", "WAVES", "MATIC",
      "ERD", "DOGE", "PERL", "DUSK", "ANKR", "WIN", "COS", "COCOS", "TROY",
      "BTT", "NPXS", "CHZ", "USDS", "ONG", "HOT", "VITE", "FTT", "EUR", "GBP",
      "TRY", "RUB", "UAH", "NGN", "USDSB", "USDS", "VAI", "BKRW", "IDRT",
      "BIDR", "BVND", "SUSD", "DAI", "AUD", "JPY", "CHR", "STPT", "BTCB",
      "ETHB", "XRPB", "BNBB", "BCHB", "LTCB", "LINKB", "XTZB", "ADA", "XMR",
      "DASH", "ETC", "NEO", "ZEC", "QTUM", "ZRX", "LINK", "BAT", "IOTA", "IOST",
      "NANO", "ONT", "VET", "THETA", "REN", "RVN", "ALGO", "ZIL", "KNC", "ATOM",
      "TFUEL", "ONE", "FTM", "ENJ", "TOMO", "BAND", "WAVES", "MATIC", "ERD",
      "DOGE", "PERL", "DUSK", "ANKR", "WIN", "COS", "COCOS", "TROY");

  /**
   * Create a HistoricalData from a DataFrame.
   *
   * @param dataFrame a DataFrame
   * @param cryptoPair crypto pair
   *
   * @return a HistoricalData
   */
  public static HistoricalData fromDataFrame(CryptoPair cryptoPair,
                                             DataFrame dataFrame) {
    List<Candle> candles = new ArrayList<>();
    for (int i = 0; i < dataFrame.size(); i++) {
      candles.add(new Candle(
          dataFrame.getLong(DATE_FIELD, i), dataFrame.getDouble(OPEN_FIELD, i),
          dataFrame.getDouble(HIGH_FIELD, i), dataFrame.getDouble(LOW_FIELD, i),
          dataFrame.getDouble(CLOSE_FIELD, i),
          dataFrame.getDouble(VOLUME_FIELD, i)));
    }

    var interval = IntervalEnum.fromInterval(
        (long)(candles.get(1).date() - candles.get(0).date()));
    return new HistoricalData(cryptoPair, candles, candles.get(0).date(),
                              interval, candles.get(candles.size() - 1).date());
  }

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
                                
    /**
     * Convert to a DataFrame.
     * @return a DataFrame
     */
    public DataFrame toDataFrame() {
      DataFrame df = new DataFrame();

      df.addField(DATE_FIELD,
                  candles.stream().mapToLong(Candle::date).toArray());
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

    /**
     * Get the total value of a wallet at a given date.
     *
     * @param wallet wallet
     * @param date date in milliseconds
     * @param targetCurrency target currency
     * @return total value
     */
    public double totalValue(Wallet wallet, long date) {
      return wallet.balances.entrySet()
          .stream()
          .mapToDouble(entry -> {
            var crypto = entry.getKey();
            var balance = entry.getValue();
            var price = price(crypto, date);
            return balance * price;
          })
          .sum();
    }

    /**
     * Get the price of a crypto at a given date.
     *
     * @param crypto crypto
     * @param date date in milliseconds
     * @param targetCurrency target currency
     * @return price
     */
    public double price(String crypto, long date) {
      // most performant way to get the price of a crypto at a given date
      // knowing that the candles are sorted by date
      var candle = candles.stream()
          .filter(c -> c.date() <= date)
          .reduce((first, second) -> second)
          .orElseThrow();
      
      return candle.close();
    }
  }

  /**
   * A trade.
   *
   * @param enter entry price
   * @param exit exit price
   * @param start start time in milliseconds
   * @param end end time in milliseconds
   */
  public record Trade(double enter, double exit, long start, long end) {

    public Trade(double enter, long start){
      this(enter, 0, start, 0);
    }

    /**
     * 
     * @return 0 if the trade is open, 1 if the trade is closed
     */
    boolean isOpen() { return end == 0; }

    /**
     * Get the duration of the trade.
     *
     * @return duration in milliseconds
     */
    long duration() { return end - start; }

    /**
     * Get the profit percentage of the trade.
     *
     * @return profit percentage
     */
    double profitPercentage() { return (exit - enter) / enter * 100; }

    /**
     * Get the profit of the trade.
     *
     * @return profit
     */
    double profit() { return exit - enter; }
  }
}
