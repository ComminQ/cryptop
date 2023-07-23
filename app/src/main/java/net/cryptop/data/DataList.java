package net.cryptop.data;

public interface DataList {

  /**
   * Get the type of the data.
   *
   * @return the type of the data
   */
  Class<?> getType();

  /**
   * Get the size of the data.
   *
   * @return the size of the data
   */
  int size();

  /**
   * Create a new DataList with the same type as this one.
   *
   * @param start the start index
   * @param end the end index (exclusive)
   * @return a new DataList
   */
  DataList subList(int start, int end);
}
