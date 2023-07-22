package net.cryptop.indicators;

import java.util.List;
import net.cryptop.App;
import net.cryptop.data.DataFrame;

/**
 * An indicator is a function that takes a DataFrame and modify it
 */
public interface Indicator {

  public static final List<Class<? extends Indicator>> NO_DEPENDENCIES =
      List.of();

  /**
   * Process the given DataFrame with the given indicators.
   *
   * @param main the DataFrame to process
   * @param indicators the indicators to apply
   */
  public static void process(DataFrame main, List<Indicator> indicators) {
    int size = main.size();
    App.getLogger().info("Processing " + size + " rows");
    for (int i = 0; i < size; i++) {
      for (var indicator : indicators) {
        int period = indicator.period();
        String fieldName = indicator.fieldName();
        if (i < period) {
          main.addValue(fieldName, Double.NaN);
          continue;
        }
        var dataFrameSubSet = main.window(i - period, i);
        if (indicator instanceof SavgolFilterIndicator) {
        }
        double value = indicator.apply(i, dataFrameSubSet);
        main.addValue(fieldName, value);
      }
    }
  }
  /**
   * Return the dependencies of this indicator.
   *
   * @return the dependencies of this indicator
   */
  default List<Class<? extends Indicator>> dependencies() {
    return NO_DEPENDENCIES;
  }

  /**
   *
   * @return the name of the field that this indicator will add to the DataFrame
   */
  String fieldName();

  /**
   * Apply the indicator to the given DataFrame
   * @param dataFrame the DataFrame to modify
   */
  double apply(int index, DataFrame dataFrameSubSet);

  /**
   * The period of the indicator
   * @return the period of the indicator
   */
  int period();
}
