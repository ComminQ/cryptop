package net.cryptop.utils.binance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import net.cryptop.App;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.data.DataClasses.Candle;
import net.cryptop.data.DataClasses.HistoricalData;

public class BinanceData {

  @Getter
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

    public String tag() {
      return tag;
    }

    public static IntervalEnum fromInterval(long difference) {
      for (var interval : IntervalEnum.values()) {
        // check if difference is equal to interval, with a 10% margin
        if (Math.abs(difference - interval.milliseconds) < interval.milliseconds * 0.1) {
          return interval;
        }
      }
      return IntervalEnum.HOURS_1;
    }

    public static IntervalEnum findByTag(String tag) {
      for (var interval : IntervalEnum.values()) {
        if (interval.tag.equals(tag)) {
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

  record ServerTime(long serverTime) {
  }

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
      // "serverTime": 1499827319559
      // }

      // extract server time
      ServerTime serverTime = client
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
   * @param pair     crypto pair
   * @param from     start time in milliseconds
   * @param interval interval
   * @return historical data
   */
  public static CompletableFuture<HistoricalData> getHistoricalData(CryptoPair pair, long from, IntervalEnum interval) {
    return getHistoricalData(pair, from, interval, System.currentTimeMillis());
  }

  /**
   * Get historical data.
   *
   * @param pair     crypto pair
   * @param from     start time in milliseconds
   * @param interval interval
   * @param to       end time in milliseconds
   * @return historical data
   */
  public static CompletableFuture<HistoricalData> getHistoricalData(CryptoPair pair, long from, IntervalEnum interval,
      long to) {
    // problem: Binance API only allows 1000 candles per request
    // solution: split request into multiple requests
    // 1. get the number of candles between from and to
    // 2. split the requests into multiple requests
    // - Eg: 10000 candles, 1000 candles for each requests
    // - Eg: 1500 candles, 1000 candles for the first request, 500 for the second
    // 3. join the results

    return CompletableFuture.supplyAsync(() -> {
      var logger = App.getLogger();

      long difference = to - from;
      long intervalDifference = interval.milliseconds;
      int totalCandles = (int) (difference / intervalDifference);

      int maxCandlesPerRequest = 1000;
      int totalRequests = (int) Math.ceil((double) totalCandles / maxCandlesPerRequest);
      int currentCandle = 0;

      long start = from;
      List<HistoricalData> historicalDataList = new ArrayList<>();

      logger.info("Downloading historical data for " + pair.symbol() + " from " + from + " to " + to + " with interval "
          + interval.tag());
      logger.info("Total candles: " + totalCandles);
      logger.info("Requests count: " + totalRequests);

      while (currentCandle < totalCandles) {
        int maxCandlesSteps = Math.min(totalCandles - currentCandle, maxCandlesPerRequest);
        long end = start + maxCandlesSteps * intervalDifference;
        if (end > to) {
          end = to;
        }
        historicalDataList.add(getHistoricalDataUnit(pair, start, interval, end, maxCandlesSteps));
        start = end;
        currentCandle += maxCandlesSteps;
      }

      // join results
      List<Candle> candles = new ArrayList<>();
      LongSet dates = new LongOpenHashSet();

      for (HistoricalData historicalData : historicalDataList) {
        for (Candle candle : historicalData.candles()) {
          if (!dates.contains(candle.date())) {
            candles.add(candle);
            dates.add(candle.date());
          }
        }
      }

      return new HistoricalData(pair, candles, from, interval, to);
    });
  }

  private static HistoricalData getHistoricalDataUnit(CryptoPair pair, long from, IntervalEnum interval,
      long to, int limit) {
    // Binance API docs:
    // https://api.binance.com/api/v3/klines
    // params:

    // Name Type Mandatory Description
    // symbol STRING YES
    // interval ENUM YES
    // startTime LONG NO
    // endTime LONG NO
    // limit INT NO Default 500; max 1000.

    // result:
    // [
    // [
    // 1499040000000, // Kline open time
    // "0.01634790", // Open price
    // "0.80000000", // High price
    // "0.01575800", // Low price
    // "0.01577100", // Close price
    // "148976.11427815", // Volume
    // 1499644799999, // Kline Close time
    // "2434.19055334", // Quote asset volume
    // 308, // Number of trades
    // "1756.87402397", // Taker buy base asset volume
    // "28.46694368", // Taker buy quote asset volume
    // "0" // Unused field, ignore.
    // ]
    // ]
    HttpClient client = HttpClient.newHttpClient();

    String url = "https://api.binance.com/api/v3/klines?symbol=" + pair.symbol() +
        "&limit=" + limit
        + "&interval=" + interval.tag() + "&startTime=" + from +
        "&endTime=" + to;

    var request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();

    // stream response
    JsonElement jsonObject = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenApply(s -> new GsonBuilder().create().fromJson(
            s, JsonElement.class))
        .join();

    var jsonArray = jsonObject.getAsJsonArray();
    // Type = JsonArray < JsonArray < JsonElement > >
    List<Candle> output = new ArrayList<>();
    // convert each jsonArray to Candle
    for (JsonElement arr : jsonArray) {
      output.add(new Candle(arr.getAsJsonArray()));
    }

    return new HistoricalData(pair, output, from, interval, to);
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
