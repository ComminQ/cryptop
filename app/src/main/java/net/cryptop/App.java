package net.cryptop;

import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.cryptop.config.Config;
import net.cryptop.running.V1;
import net.cryptop.running.V2;
import net.cryptop.utils.ChronoUtils;
import net.cryptop.utils.IOUtils;
import net.cryptop.utils.binance.BinanceData;

public class App {

  public static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");
  private static final Logger logger;

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format",
        "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    logger = Logger.getLogger(App.class.getName());
  }

  public static Logger getLogger() {
    return logger;
  }

  public static void main(String[] args) throws Exception {

    Options options = new Options();

    Option version = new Option("v", "version", true, "Select the version of the program to run");
    version.setOptionalArg(true);
    version.setType(String.class);
    options.addOption(version);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;

    try {
      cmd = parser.parse(options, args);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);

      System.exit(1);
      return;
    }

    Config config = ChronoUtils.time("Loading config", () -> {
      return Config.loadConfig().orElseGet(() -> {
        // if config.json does not exist, create it
        System.out.println("Creating default config.json ...");
        Config defaultConfig = Config.defaultConfig();
        System.out.print("Enter your Binance API key: ");
        String apiKey = IOUtils.readStdIn();
        System.out.print("Enter your Binance secret key: ");
        String secretKey = IOUtils.readStdIn();
        defaultConfig.setMainCredentials(
            new Config.BinanceCredentials(apiKey, secretKey));

        defaultConfig.saveConfig();
        return defaultConfig;
      });
    });

    logger.info("Config loaded: " + config);
    if (config.getTimeSettings().startDate() == 0L) {
      System.out.println("Error in config.json: startDate is not set. "
          + "Please edit config.json and set startDate to a valid timestamp (in milliseconds).");
      System.exit(1);
    }

    var pairs = config.getPairs();
    logger.info("Loaded " + pairs.size() + " pairs:");
    pairs.forEach(pair -> logger.info("  - " + pair));

    // download data from Binance
    for (var pair : pairs) {
      boolean hasBeenDownloaded = BinanceData.hasBeenDownloaded(pair);
      if (hasBeenDownloaded) {
        // ask user if they want to download again
        System.out.print("Data for " + pair + " has already been downloaded. "
            + "Do you want to download again? (y/n) ");
        String answer = IOUtils.readStdIn();
        if (!answer.equals("y")) {
          continue;
        }
      }
      logger.info("Downloading data for " + pair + " ...");
      long from = config.getTimeSettings().startDate();
      var historicalData = BinanceData
          .getHistoricalData(pair, from, config.getTimeSettings().interval())
          .join();
      logger.info("Downloaded " + historicalData.size() + " candles.");

      logger.info("Saving data to CSV ...");
      // save data to CSV
      var dataFrame = historicalData.toDataFrame();
      dataFrame.saveToCSV("data/" + pair.symbol() + ".csv");
      logger.info("Saved data to CSV.");
    }

    String versionToRun = cmd.getOptionValue("version", "v1").toLowerCase();
    switch (versionToRun) {
      case "v1":
        V1.execute(config);
        break;
      case "v2":
        V2.execute(config);
        break;
      default:
        System.out.println("Invalid version: " + versionToRun);
        System.exit(1);
        return;
    }

  }
}
