package net.cryptop.utils;

public class MathUtils {

  /**
   * Return the nearest odd number to the given number.
   *
   * @param number window size
   * @return nearest odd number
   */
  public static int nearestOdd(int number) {
    return number % 2 == 0 ? number - 1 : number;
  }

  /**
   * 
   * @param data
   * @return
   */
  public static double max(double[] data) {
    double max = Double.MIN_VALUE;
    for (double d : data) {
      if (d > max) {
        max = d;
      }
    }
    return max;
  }

  /**
   * 
   * @param data
   * @param max
   * @return
   */
  public static double[] divide(double[] data, double max) {
    double[] result = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      result[i] = data[i] / max;
    }
    return result;
  }
}
