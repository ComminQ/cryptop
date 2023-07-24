package net.cryptop.running;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.cryptop.App;
import net.cryptop.config.Config;
import net.cryptop.data.DataClasses;
import net.cryptop.data.DataFrame;
import net.cryptop.indicators.Indicator;
import net.cryptop.wallet.Wallet;

public class V1 {

    // French date format
    public static DateTimeFormatter FORMATTED = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void execute(Config config) {
        System.out.println("===== Starting App =====");
        var logger = App.getLogger();
        // read config from config.json
        var pairs = config.getPairs();

        logger.info("Applying indicators ...");
        for (var pair : pairs) {
            // load inital data
            var dataFrame = DataFrame.loadCSV("data/" + pair.symbol() + ".csv");
            logger.info("Applying indicators to " + pair + " ...");
            for (var indicator : config.getIndicators()) {
                logger.info("  - " + indicator);
            }
            // apply indicators
            Indicator.process(dataFrame, config.getIndicators());
            // save data to CSV
            dataFrame.saveToCSV("data/" + pair.symbol() + "_indicators.csv");
        }

        logger.info("Starting backtesting ...");

        for (var pair : pairs) {
            // load data
            var dataFrame = DataFrame.loadCSV("data/" + pair.symbol() + "_indicators.csv");
            // backtest
            logger.info("Testing strategies on " + pair + " (50 " +
                    pair.stableCoin() + ")...");
            for (var strategy : config.getStrategies()) {
                logger.info("  - " + strategy.getName());
            }

            for (var strategy : config.getStrategies()) {
                dataFrame.freezeFinanceFields();
                var historicalData = dataFrame.toHistoricalData(pair);
                logger.info("Testing " + strategy.getName() + " on " + pair + " ...");
                // run strategy
                Wallet initialWallet = new Wallet();
                initialWallet.setBalance(pair.stableCoin(), 50.0);

                var result = strategy.run(initialWallet.clone(), historicalData, dataFrame);
                var finalWallet = result.wallet();
                var trades = result.trades();
                // compare initial and final wallet
                // var initialBalance = initialWallet.getBalance(pair.stableCoin());
                // var finalBalance = finalWallet.getBalance(pair.stableCoin());
                long firstDate = dataFrame.getLong(DataClasses.DATE_FIELD, 0);
                long lastDate = dataFrame.getLong(DataClasses.DATE_FIELD, dataFrame.size() - 1);
                long duration = lastDate - firstDate;

                var firstDateStr = ZonedDateTime.ofInstant(new Date(firstDate).toInstant(), ZoneId.systemDefault())
                        .format(FORMATTED);
                var lastDateStr = ZonedDateTime.ofInstant(new Date(lastDate).toInstant(), ZoneId.systemDefault())
                        .format(FORMATTED);

                logger.info("  - Duration: from " + firstDateStr + " to " + lastDateStr + " "
                        + (duration / 1000 / 60 / 60 / 24) + " days, with interval " +
                        config.getTimeSettings().interval().tag() + " (" + duration + " ms)");

                var initialWalletValue = initialWallet.getValue(firstDate, historicalData);
                var finalWalletValue = finalWallet.getValue(lastDate, historicalData);

                var profit = finalWalletValue - initialWalletValue;
                double profitPercent = profit / initialWalletValue * 100;
                var periodOfTimeInDays = duration / 1000 / 60 / 60 / 24;
                double profitPerYear = profitPercent / periodOfTimeInDays * 365;

                logger.info("  - Initial balance: " + initialWalletValue + " " +
                        pair.stableCoin());
                logger.info("  - Final balance: " + finalWalletValue + " " +
                        pair.stableCoin());
                String profitStr = profit > 0 ? "+" + profit : "" + profit;
                logger.info("  - Profit: " + profitStr + " " + pair.stableCoin());
                logger.info(
                        "  - Profit (%): " + App.DEC_FORMAT.format(profitPercent) + "%, averaging "
                                + App.DEC_FORMAT.format(profitPerYear)
                                + "% per year");
                logger.info("  - Trades: " + trades.size());
                // save results
                logger.info("Saving results to CSV ...");
                String csvFileName = "results/" + pair.symbol() + "_" + strategy.getName() + ".csv";
                var resultsDataFrame = result.toDataFrame(historicalData);
                resultsDataFrame.saveToCSV(csvFileName);
            }
        }
    }

}
