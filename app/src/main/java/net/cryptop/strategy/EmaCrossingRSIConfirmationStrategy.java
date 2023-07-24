package net.cryptop.strategy;

import net.cryptop.data.DataFrame;

public class EmaCrossingRSIConfirmationStrategy extends BuySellSignalsStrategy {

    private String shortEma;
    private String longEma;
    private String rsiPeriod;
    private double rsiLowThreshold;
    private double rsiHighThreshold;

    /**
     * @param shortSma
     * @param longSma
     * @param rsiPeriod
     * @param rsiLowThreshold
     * @param rsiHighThreshold
     */
    public EmaCrossingRSIConfirmationStrategy(String shortEma, String longEma, String rsiPeriod, double rsiLowThreshold,
            double rsiHighThreshold) {
        this.shortEma = shortEma;
        this.longEma = longEma;
        this.rsiPeriod = rsiPeriod;
        this.rsiLowThreshold = rsiLowThreshold;
        this.rsiHighThreshold = rsiHighThreshold;
    }

    public int getShortEma() {
        return Integer.parseInt(shortEma.replace("EMA", ""));
    }

    public int getLongEma() {
        return Integer.parseInt(longEma.replace("EMA", ""));
    }

    public int getRsiPeriod() {
        return Integer.parseInt(rsiPeriod.replace("RSI", ""));
    }

    public double getRsiLowThreshold() {
        return rsiLowThreshold;
    }

    public double getRsiHighThreshold() {
        return rsiHighThreshold;
    }

    @Override
    public String getName() {
        return "EMA_CROSSING_RSI_CONFIRMATION";
    }

    @Override
    public boolean buySignal(DataFrame dataFrame, double currentCrypto, long currenDtate, double currentStableCoin,
            int index) {
        var shortEmaVal = dataFrame.getDouble(this.shortEma, index);
        var longEmaVal = dataFrame.getDouble(this.longEma, index);
        var rsi = dataFrame.getDouble(this.rsiPeriod, index);

        return shortEmaVal > longEmaVal && rsi < rsiLowThreshold;
    }

    @Override
    public boolean sellSignal(DataFrame dataFrame, double currentCrypto, long currenDtate, double currentStableCoin,
            int index) {
        var shortEmaVal = dataFrame.getDouble(this.shortEma, index);
        var longEmaVal = dataFrame.getDouble(this.longEma, index);
        var rsi = dataFrame.getDouble(this.rsiPeriod, index);

        return shortEmaVal < longEmaVal || rsi > rsiHighThreshold;
    }

}
