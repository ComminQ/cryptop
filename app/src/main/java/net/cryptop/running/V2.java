package net.cryptop.running;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.ta4j.core.BacktestExecutor;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Indicator;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Trade.TradeType;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.reports.PerformanceReport;
import org.ta4j.core.reports.PositionStatsReport;
import org.ta4j.core.reports.TradingStatement;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;

import net.cryptop.App;
import net.cryptop.config.Config;
import net.cryptop.strategy.EmaCrossingRSIConfirmationStrategy;
import net.cryptop.strategy.SmaCrossingStrategy;
import net.cryptop.utils.TA4JUtils;

public class V2 {

    public static void execute(Config config) {
        var pairs = config.getPairs();
        var logger = App.getLogger();

        for (var pair : pairs) {
            BarSeries series = TA4JUtils.loadCsv(pair, "data/" + pair.symbol() + ".csv");
            var quoteAmount = 50;
            var currentCryptoValue = series.getBar(0).getClosePrice().doubleValue();
            var cryptoAmount = quoteAmount / currentCryptoValue;

            List<Strategy> strategies = new ArrayList<>();

            for (var strategy : config.getStrategies()) {
                if (strategy instanceof SmaCrossingStrategy smaCrossingStrategy) {
                    int shortPeriod = smaCrossingStrategy.getShortSmaPeriod();
                    int longPeriod = smaCrossingStrategy.getLongSmaPeriod();

                    strategies.add(createSmaCrossingStrategy(series, shortPeriod, longPeriod));

                } else if (strategy instanceof EmaCrossingRSIConfirmationStrategy emaCrossingStrategy) {
                    int shortPeriod = emaCrossingStrategy.getShortEma();
                    int longPeriod = emaCrossingStrategy.getLongEma();
                    int rsiPeriod = emaCrossingStrategy.getRsiPeriod();
                    double rsiLowThreshold = emaCrossingStrategy.getRsiLowThreshold();
                    double rsiHighThreshold = emaCrossingStrategy.getRsiHighThreshold();

                    strategies.add(createEmaCrossingRSIConfirmationStrategy(series, shortPeriod, longPeriod, rsiPeriod,
                            rsiLowThreshold, rsiHighThreshold));
                }
            }

            Instant startInstant = Instant.now();
            BacktestExecutor executor = new BacktestExecutor(series);
            List<TradingStatement> tradingStatements = executor.execute(strategies, DecimalNum.valueOf(cryptoAmount),
                    TradeType.BUY);
            logger.info("Back-tested " + strategies.size() + " strategies on " + series.getBarCount()
                    + "-bar series in " + Duration.between(startInstant, Instant.now()));
            System.out.println(printReport(tradingStatements));

        }

    }

    private static String printReport(List<TradingStatement> tradingStatements) {
        StringJoiner resultJoiner = new StringJoiner(System.lineSeparator());
        for (TradingStatement statement : tradingStatements) {
            resultJoiner.add(printStatementReport(statement).toString());
        }

        return resultJoiner.toString();
    }

    private static StringBuilder printStatementReport(TradingStatement statement) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("######### ")
                .append(statement.getStrategy().getName())
                .append(" #########")
                .append(System.lineSeparator())
                .append(printPerformanceReport(statement.getPerformanceReport()))
                .append(System.lineSeparator())
                .append(printPositionStats(statement.getPositionStatsReport()))
                .append(System.lineSeparator())
                .append("###########################");
        return resultBuilder;
    }

    private static StringBuilder printPerformanceReport(PerformanceReport report) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("--------- performance report ---------")
                .append(System.lineSeparator())
                .append("total loss: ")
                .append(report.getTotalLoss())
                .append(System.lineSeparator())
                .append("total profit: ")
                .append(report.getTotalProfit())
                .append(System.lineSeparator())
                .append("total profit loss: ")
                .append(report.getTotalProfitLoss())
                .append(System.lineSeparator())
                .append("total profit loss percentage: ")
                .append(report.getTotalProfitLossPercentage())
                .append(System.lineSeparator())
                .append("---------------------------");
        return resultBuilder;
    }

    private static StringBuilder printPositionStats(PositionStatsReport report) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("--------- trade statistics report ---------")
                .append(System.lineSeparator())
                .append("loss trade count: ")
                .append(report.getLossCount())
                .append(System.lineSeparator())
                .append("profit trade count: ")
                .append(report.getProfitCount())
                .append(System.lineSeparator())
                .append("break even trade count: ")
                .append(report.getBreakEvenCount())
                .append(System.lineSeparator())
                .append("---------------------------");
        return resultBuilder;
    }

    private static Strategy createEmaCrossingRSIConfirmationStrategy(BarSeries series, int shortPeriod, int longPeriod,
            int rsiPeriod, double rsiLowThreshold, double rsiHighThreshold) {
        return new BaseStrategy("EMA_Crossing_RSI",
                createEmaCrossEntryRule(series, shortPeriod, longPeriod, rsiPeriod, rsiLowThreshold, rsiHighThreshold),
                createEmaCrossExitRule(series, shortPeriod, longPeriod, rsiPeriod, rsiLowThreshold, rsiHighThreshold));
    }

    // Entry Rules:
    // Buy Signal: When the EMA-50 crosses above the EMA-200, and the RSI is below
    // 70 (not in overbought territory), it generates a buy signal.
    // Sell Signal: When the EMA-50 crosses below the EMA-200, or the RSI goes above
    // 70 (in overbought territory), it generates a sell signal.
    //
    // Trading Rules:
    // Only enter a trade when both the EMA crossover and RSI confirmation
    // conditions are met.
    // Place a stop-loss order below the recent swing low for buy trades and above
    // the recent swing high for sell trades to limit potential losses.
    // Set a target price based on your risk-reward ratio and overall market
    // conditions. For example, aim for a 1:2 risk-reward ratio, where the potential
    // profit is twice the size of the stop-loss.
    // If a trade reaches the target price, close the position. If the stop-loss is
    // hit, exit the trade and wait for the next valid signal.
    // Avoid entering trades during times of high market volatility or major news
    // events, as it may lead to false signals.
    // Continuously monitor the EMA crossover and RSI conditions to manage open
    // positions and adjust stop-loss and take-profit levels as needed.

    private static Rule createEmaCrossEntryRule(BarSeries series, int shortPeriod, int longPeriod, int rsiPeriod,
            double rsiLowThreshold, double rsiHighThreshold) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator emaShort = new EMAIndicator(closePrice, shortPeriod);
        EMAIndicator emaLong = new EMAIndicator(closePrice, longPeriod);
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiPeriod);

        return new CrossedUpIndicatorRule(emaShort, emaLong)
                .and(new CrossedDownIndicatorRule(rsi, DecimalNum.valueOf(rsiHighThreshold)));
    }

    private static Rule createEmaCrossExitRule(BarSeries series, int shortPeriod, int longPeriod, int rsiPeriod,
            double rsiLowThreshold, double rsiHighThreshold) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator emaShort = new EMAIndicator(closePrice, shortPeriod);
        EMAIndicator emaLong = new EMAIndicator(closePrice, longPeriod);
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiPeriod);

        return new CrossedDownIndicatorRule(emaShort, emaLong)
                .or(new CrossedUpIndicatorRule(rsi, DecimalNum.valueOf(rsiLowThreshold)))
                .or(new StopGainRule(closePrice, DecimalNum.valueOf("2")));
    }

    public static Strategy createSmaCrossingStrategy(BarSeries barSeries, int shortPeriod, int longPeriod) {
        return new BaseStrategy("SMA_Crossing_" + shortPeriod + "_" + longPeriod,
                createSmaCrossEntryRule(barSeries, shortPeriod, longPeriod),
                createSmaCrossExitRule(barSeries, shortPeriod, longPeriod));
    }

    private static Rule createSmaCrossEntryRule(BarSeries series, int shortBarCount, int longBarCount) {
        Indicator<Num> closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaShort = new SMAIndicator(closePrice, shortBarCount);
        SMAIndicator smaLong = new SMAIndicator(closePrice, longBarCount);

        return new CrossedUpIndicatorRule(smaShort, smaLong);
    }

    private static Rule createSmaCrossExitRule(BarSeries series, int shortBarCount, int longBarCount) {
        Indicator<Num> closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaShort = new SMAIndicator(closePrice, shortBarCount);
        SMAIndicator smaLong = new SMAIndicator(closePrice, longBarCount);

        return new CrossedDownIndicatorRule(smaShort, smaLong);
    }

}
