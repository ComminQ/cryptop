package net.cryptop.utils.binance;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.data.DataClasses.Candle;
import net.cryptop.data.DataClasses.HistoricalData;

public class BinanceData {

  public enum IntervalEnum {
    SECONDS_1("1s", 1000),
    MINUTES_1("1m", 60 * 1000),
    MINUTES_3("3m", 180000),
    MINUTES_5("5m", 300000),
    MINUTES_15("15m", 900000),
    MINUTES_30("30m", 1800000),
    HOURS_1("1h", 3600000),
    HOURS_2("2h", 7200000),
    HOURS_4("4h", 14400000),
    HOURS_6("6h", 21600000),
    HOURS_8("8h", 28800000),
    HOURS_12("12h", 43200000),
    DAYS_1("1d", 86400000),
    DAYS_3("3d", 259200000),
    WEEK_1("1w", 604800000),
    MONTH_1("1M", 2592000000L);

    final String tag;
    final long milliseconds;

    IntervalEnum(String tag, long milliseconds) {
      this.tag = tag;
      this.milliseconds = milliseconds;
    }

    public String tag() { return tag; }

    public static IntervalEnum fromInterval(long difference) {
      for (var interval : IntervalEnum.values()) {
        // check if difference is equal to interval, with a 10% margin
        if (Math.abs(difference - interval.milliseconds) <
            interval.milliseconds * 0.1) {
          return interval;
        }
      }
      return IntervalEnum.HOURS_1;
    }
  }

  /**
   * Ping Binance API.
   *
   * @return true if successful
   */
  public static CompletableFuture<Boolean> ping() {
    return CompletableFuture.supplyAsync(() -> {
      HttpClient client = HttpClient.newHttpClient();
      String url = "https://api.binance.com/api/v3/ping";
      // send request
      client
          .sendAsync(HttpRequest.newBuilder().uri(URI.create(url)).build(),
                     HttpResponse.BodyHandlers.ofString())
          .thenApply(HttpResponse::body)
          .thenAccept(System.out::println)
          .join();
      return true;
    });
  }

  record ServerTime(long serverTime) {}

  /**
   * Get server time.
   *
   * @return server time in milliseconds
   */
  public static CompletableFuture<Long> serverTime() {
    return CompletableFuture.supplyAsync(() -> {
      HttpClient client = HttpClient.newHttpClient();
      String url = "https://api.binance.com/api/v3/time";
      // response format:
      // {
      //   "serverTime": 1499827319559
      // }

      // extract server time
      ServerTime serverTime =
          client
              .sendAsync(HttpRequest.newBuilder().uri(URI.create(url)).build(),
                         HttpResponse.BodyHandlers.ofString())
              .thenApply(HttpResponse::body)
              .thenApply(
                  s -> new GsonBuilder().create().fromJson(s, ServerTime.class))
              .join();

      return serverTime.serverTime();
    });
  }

  /**
   * Get historical data.
   *
   * @param pair crypto pair
   * @param from start time in milliseconds
   * @param interval interval
   * @return historical data
   */
  public static CompletableFuture<HistoricalData>
  getHistoricalData(CryptoPair pair, long from, IntervalEnum interval) {
    return getHistoricalData(pair, from, interval, System.currentTimeMillis());
  }

  /**
   * Get historical data.
   *
   * @param pair crypto pair
   * @param from start time in milliseconds
   * @param interval interval
   * @param to end time in milliseconds
   * @return historical data
   */
  public static CompletableFuture<HistoricalData>
  getHistoricalData(CryptoPair pair, long from, IntervalEnum interval,
                    long to) {
    // Binance API docs:
    // https://api.binance.com/api/v3/klines
    // params:

    // Name	Type	Mandatory	Description
    // symbol	STRING	YES
    // interval	ENUM	YES
    // startTime	LONG	NO
    // endTime	LONG	NO
    // limit	INT	NO	Default 500; max 1000.

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

    return CompletableFuture
        .supplyAsync(() -> {
          HttpClient client = HttpClient.newHttpClient();

          String url =
              "https://api.binance.com/api/v3/klines?symbol=" + pair.symbol() +
              "&limit=1000"
              + "&interval=" + interval.tag() + "&startTime=" + from +
              "&endTime=" + to;

          var request =
              HttpRequest.newBuilder().GET().uri(URI.create(url)).build();

          // stream response
          JsonElement jsonObject =
              client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                  .thenApply(HttpResponse::body)
                  .thenApply(s
                             -> new GsonBuilder().create().fromJson(
                                 s, JsonElement.class))
                  .join();

          var jsonArray = jsonObject.getAsJsonArray();
          return jsonArray;
        })
        .thenApply(jsonArray -> {
          // Type = JsonArray < JsonArray < JsonElement > >
          List<Candle> output = new ArrayList<>();
          // convert each jsonArray to Candle
          for (JsonElement arr : jsonArray) {
            output.add(new Candle(arr.getAsJsonArray()));
          }

          return new HistoricalData(pair, output, from, interval, to);
        });
  }

  /**
   * Check if historical data has been downloaded.
   *
   * @param pair crypto pair
   * @return true if historical data has been downloaded
   */
  public static boolean hasBeenDownloaded(CryptoPair pair) {
    String fileName = pair.symbol() + ".csv";
    String dir = "data/";
    return new java.io.File(dir + fileName).exists();
  }
}
