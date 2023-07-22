package net.cryptop.indicators;

import com.github.psambit9791.jdsp.filter.Savgol;
import net.cryptop.data.DataClasses;
import net.cryptop.data.DataFrame;
import net.cryptop.utils.MathUtils;

/**
 * Smoothing filter
 *
 */
public class SavgolFilterIndicator implements Indicator {

  private int windowSize;
  private Savgol savgol;

  /**
   * @param windowSize
   */
  public SavgolFilterIndicator(int windowSize, int polynomialOrder) {
    this.savgol = new Savgol(MathUtils.nearestOdd(windowSize), polynomialOrder);
    this.windowSize = windowSize;
    this.savgol.savgolCoeffs();
  }

  @Override
  public String fieldName() {
    return "Savgol";
  }

  @Override
  public double apply(int index, DataFrame dataFrameSubSet) {
    double[] data = dataFrameSubSet.get(DataClasses.CLOSE_FIELD);
    double[] smoothed = savgol.filter(data, "mirror");
    return smoothed[smoothed.length - 1];
  }

  @Override
  public int period() {
    return windowSize;
  }

  @Override
  public String toString() {
    return "Savgol " + windowSize;
  }
}
