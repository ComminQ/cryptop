package net.cryptop.indicators;

import net.cryptop.data.DataFrame;

public class IchimokuIndicators implements Indicator {

    public static IchimokuIndicators crypto() {
        return new IchimokuIndicators(20, 60, 120);
    }

    public static IchimokuIndicators forex() {
        return new IchimokuIndicators(9, 26, 52);
    }

    private int period;
    private int period2;
    private int period3;

    /**
     * @param period
     * @param period2
     * @param period3
     */
    public IchimokuIndicators(int period, int period2, int period3) {
        this.period = period;
        this.period2 = period2;
        this.period3 = period3;
    }

    @Override
    public String fieldName() {
        return "ichimoku";
    }

    // ligne de conversion (tenkan-sen) : moyenne mobile sur 9 périodes
    // ligne de base (kijun-sen) : moyenne mobile sur 26 périodes
    // Médiane A (Senkou Span A) : moyenne mobile des lignes de conversion et de
    // base, projetée sur 26 périodes
    // Médiane B (Senkou Span B) : moyenne mobile sur 52 périodes, projetée 26
    // périodes en avant
    // La traine (Chikou Span) : cours de clôture, projeté 26 périodes en arrière

    // (20, 60, 120)

    @Override
    public double apply(int index, DataFrame dataFrameSubSet) {
        return 0;
    }

    @Override
    public int period() {
        return 120;
    }

}
