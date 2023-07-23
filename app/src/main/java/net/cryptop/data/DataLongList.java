package net.cryptop.data;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class DataLongList implements DataList {

  private LongList data;

  public DataLongList() { this.data = new LongArrayList(); }

  /**
   * @param data
   */
  public DataLongList(LongList data) { this.data = data; }

  @Override
  public Class<?> getType() {
    return long.class;
  }

  public void add(long value) { data.add(value); }

  public long get(int index) { return data.getLong(index); }

  public int size() { return data.size(); }

  public long[] toArray() { return data.toLongArray(); }

  public void clear() { data.clear(); }

  public void addAll(long[] values) { data.addElements(size() - 1, values); }

  public void addAll(DataLongList values) { data.addAll(values.data); }

  @Override
  public DataList subList(int start, int end) {
    return new DataLongList(data.subList(start, end));
  }
}
