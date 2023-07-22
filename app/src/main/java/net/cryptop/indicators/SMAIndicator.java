package net.cryptop.indicators;

import net.cryptop.data.DataClasses;
import net.cryptop.data.DataFrame;

/**
 * Simple Moving Average indicator.
 *
 * Operation:
 * Calculate the average of the last N candles.
 *
 */
public class SMAIndicator implements Indicator {

  private int period;

  public SMAIndicator(int period) { this.period = period; }

  @Override
  public String fieldName() {
    return "SMA" + period;
  }

  @Override
  public double apply(int index, DataFrame dataFrameSubSet) {
    double sum = 0;
    for (int i = 0; i < period; i++) {
      sum += dataFrameSubSet.get(DataClasses.CLOSE_FIELD, i);
    }
    return sum / period;
  }

  @Override
  public int period() {
    return period;
  }
}
