package net.cryptop.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import lombok.Getter;
import lombok.Setter;
import net.cryptop.indicators.Indicator;
import net.cryptop.indicators.IndicatorFactory;
import net.cryptop.strategy.Strategy;
import net.cryptop.strategy.StrategyFactory;
import net.cryptop.utils.binance.BinanceData.IntervalEnum;

@Getter
public class Config {

  public static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  public record TimeSettings(IntervalEnum interval, long startDate) {
  }

  public record CryptoPair(String base, String quote) {
    public CryptoPair(String symbolWithDash) {
      this(symbolWithDash.split("_")[0], symbolWithDash.split("_")[1]);
    }

    public String symbol() {
      return base + quote;
    }

    public String symbolWithDash() {
      return base + "_" + quote;
    }

    public String crypto() {
      return base;
    }

    public String stableCoin() {
      return quote;
    }
  }

  public record BinanceCredentials(String apiKey, String secretKey) {
  }

  /**
   *
   * @return Un {@link Gson} avec les parametres de
   *         serialisation/deserialisation pour la configuration.
   */
  public static Gson gson() {
    return new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.STATIC)
        .registerTypeAdapter(CryptoPair.class, cryptoPairDeserializer())
        .registerTypeAdapter(CryptoPair.class, cryptoPairSerializer())
        .registerTypeAdapter(Indicator.class, indicatorDeserializer())
        .registerTypeAdapter(Strategy.class, strategyDeserializer())
        .registerTypeAdapter(TimeSettings.class, timeSettingsDeserializer())
        .create();
  }

  /**
   *
   * @return Un deserialiseur de {@link CryptoPair}.
   */
  public static JsonDeserializer<CryptoPair> cryptoPairDeserializer() {
    return (json, typeOfT, context) -> new CryptoPair(json.getAsString());
  }

  /**
   *
   * @return Un serialiseur de {@link CryptoPair}.
   */
  public static JsonSerializer<CryptoPair> cryptoPairSerializer() {
    return (src, typeOfSrc, context) -> context.serialize(src.symbolWithDash());
  }

  /**
   *
   * @return Un deserialiseur de {@link Indicator}.
   */
  public static JsonDeserializer<Indicator> indicatorDeserializer() {
    return (json, typeOfT, context) -> {
      var jsonObj = json.getAsJsonObject();
      var indicatorName = jsonObj.get("name").getAsString();

      Map<String, String> params = new HashMap<>();
      if (jsonObj.has("params")) {
        var indicatorParams = jsonObj.get("params").getAsJsonObject();
        params = indicatorParams.entrySet().stream().collect(
            HashMap::new,
            (m, e) -> m.put(e.getKey(), e.getValue().getAsString()),
            HashMap::putAll);
      }

      return IndicatorFactory.createIndicator(indicatorName, params);
    };
  }

  public static JsonDeserializer<TimeSettings> timeSettingsDeserializer() {
    return (json, typeOfT, context) -> {
      var jsonObj = json.getAsJsonObject();
      var interval = jsonObj.get("interval").getAsString();
      // Format: YYYY-MM-DD
      if (jsonObj.has("startTime")) {
        String startDate = jsonObj.get("startTime").getAsString();
        long startDateLong = 0;
        try {
          startDateLong = DATE_TIME_FORMATTER.parse(startDate).getTime();
        } catch (Exception e) {
          e.printStackTrace();
        }
        return new TimeSettings(IntervalEnum.findByTag(interval), startDateLong);
      } else if (jsonObj.has("unitCount")) {
        int unitCount = jsonObj.get("unitCount").getAsInt();
        var timeInterval = IntervalEnum.findByTag(interval);
        long startDate = System.currentTimeMillis() - unitCount * timeInterval.getMilliseconds();

        return new TimeSettings(timeInterval, startDate);
      } else {
        return new TimeSettings(IntervalEnum.findByTag(interval), 0);
      }
    };
  }

  public static JsonDeserializer<Strategy> strategyDeserializer() {
    return (json, typeOfT, context) -> {
      var jsonObj = json.getAsJsonObject();
      var strategyName = jsonObj.get("name").getAsString();

      Map<String, String> params = new HashMap<>();
      if (jsonObj.has("params")) {
        var strategyParams = jsonObj.get("params").getAsJsonObject();
        params = strategyParams.entrySet().stream().collect(
            HashMap::new,
            (m, e) -> m.put(e.getKey(), e.getValue().getAsString()),
            HashMap::putAll);
      }

      return StrategyFactory.createStrategy(strategyName, params);
    };
  }

  /**
   *
   * @return La configuration de l'application, ou {@link Optional#empty()} si
   *         le fichier de configuration n'existe pas.
   */
  public static Optional<Config> loadConfig() {
    try (var reader = new FileReader("./config.json")) {
      Gson gson = gson();
      var config = gson.fromJson(reader, Config.class);
      return Optional.of(config);
    } catch (FileNotFoundException e) {
      return Optional.empty();
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static Config defaultConfig() {
    return new Config();
  }

  private TimeSettings timeSettings;

  private List<CryptoPair> pairs = new ArrayList<>();

  private List<Indicator> indicators = new ArrayList<>();

  private List<Strategy> strategies = new ArrayList<>();

  @Setter
  private BinanceCredentials mainCredentials;

  @Setter
  private BinanceCredentials testnetCredentials;

  private Config() {
  }

  public void saveConfig() {
    Gson gson = gson();
    String json = gson.toJson(this);
    try (var writer = new java.io.FileWriter("config.json")) {
      writer.write(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
