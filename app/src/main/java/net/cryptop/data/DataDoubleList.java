package net.cryptop.data;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DataDoubleList implements DataList {

  private DoubleList data;

  /**
   * Create a DoubleList.
   */
  public DataDoubleList() { this.data = new DoubleArrayList(); }

  /**
   * Create a DoubleList from a DoubleList.
   *
   * @param data a DoubleList
   */
  public DataDoubleList(DoubleList data) { this.data = data; }

  public void add(double value) { data.add(value); }

  public double get(int index) { return data.getDouble(index); }

  public int size() { return data.size(); }

  public double[] toArray() { return data.toDoubleArray(); }

  public void clear() { data.clear(); }

  public void addAll(double[] values) { data.addElements(size() - 1, values); }

  public void addAll(DataDoubleList values) { data.addAll(values.data); }

  @Override
  public Class<?> getType() {
    return double.class;
  }

  @Override
  public DataList subList(int start, int end) {
    return new DataDoubleList(data.subList(start, end));
  }
}
