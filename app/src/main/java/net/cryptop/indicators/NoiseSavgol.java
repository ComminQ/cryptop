package net.cryptop.indicators;

import net.cryptop.data.DataClasses;
import net.cryptop.data.DataFrame;

/**
 * Use the SavgolFilterIndicator to extract the main trend from the noise of the
 * market.
 *
 */
public class NoiseSavgol implements Indicator {

  @Override
  public String fieldName() {
    return "NoiseSavgol";
  }

  @Override
  public double apply(int index, DataFrame dataFrameSubSet) {
    double[] data = dataFrameSubSet.getDoubles("Savgol");
    return dataFrameSubSet.getDouble(DataClasses.CLOSE_FIELD, data.length - 1) -
        data[data.length - 1];
  }

  @Override
  public int period() {
    return 50;
  }

  @Override
  public String toString() {
    return "NoiseSavgol";
  }
}
