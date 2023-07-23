package net.cryptop.indicators;

import net.cryptop.data.DataClasses;
import net.cryptop.data.DataFrame;

/**
 * Exponential Moving Average indicator.
 *
 * Operation:
 * Current EMA= ((Price(current) - previous EMA)) X multiplier) + previous EMA.
 */
public class EMAIndicator implements Indicator {

  private int period;
  private double multiplier;

  public EMAIndicator(int period) {
    this.period = period;
    this.multiplier = 2.0 / (period + 1);
  }

  @Override
  public String fieldName() {
    return "EMA" + period;
  }

  @Override
  public double apply(int index, DataFrame dataFrameSubSet) {
    int size = dataFrameSubSet.size();
    var close = dataFrameSubSet.getDouble(DataClasses.CLOSE_FIELD, size - 1);
    var ema = dataFrameSubSet.getDouble(fieldName(), size - 2);
    if (Double.isNaN(ema)){
      // return sma if ema is not available
      return new SMAIndicator(period).apply(index, dataFrameSubSet);
    }
    return (close - ema) * this.multiplier + ema;
  }

  @Override
  public int period() {
    return period;
  }

  @Override
  public String toString() {
    return "EMA" + period;
  }
}
