package net.cryptop.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Getter;
import net.cryptop.config.Config.CryptoPair;
import net.cryptop.data.DataClasses.HistoricalData;
import net.cryptop.utils.file.CSVUtils;

@Getter
public class DataFrame {

  /**
   * Load a CSV file.
   *
   * @param fileName file name
   * @return a DataFrame
   */
  public static DataFrame loadCSV(String fileName) {
    return CSVUtils.loadCSV(fileName);
  }

  /**
   * Map of field name to list of values.
   */
  private Map<String, DataList> data = new HashMap<>();

  /**
   * Map of field name to type.
   */
  private Map<String, Class<?>> types = new HashMap<>();

  /**
   * List of field names in order.
   */
  private List<String> fieldOrders = new ArrayList<>();

  /**
   * Set of frozen fields.
   */
  private Set<String> frozenFields = new HashSet<>();

  /**
   * Get the type of a field.
   *
   * @param field field name
   * @return the type of the field
   */
  public Class<?> getType(String field) {
    return types.get(field);
  }

  /**
   * Set the type of a field.
   *
   * @param field field name
   * @param type  type
   */
  public void setType(String field, Class<?> type) {
    types.put(field, type);
  }

  public void freezeField(String field) {
    frozenFields.add(field);
  }

  public void freezeFields(String... fields) {
    for (String field : fields) {
      freezeField(field);
    }
  }

  public void freezeFinanceFields(){
    freezeFields(
      DataClasses.CLOSE_FIELD,
      DataClasses.HIGH_FIELD,
      DataClasses.LOW_FIELD,
      DataClasses.OPEN_FIELD,
      DataClasses.VOLUME_FIELD,
      DataClasses.DATE_FIELD
    );
  }

  private boolean isFrozen(String field) {
    return frozenFields.contains(field);
  }

  private void checkFrozen(String field) {
    if (isFrozen(field))
      throw new IllegalArgumentException("Field " + field + " is frozen");
  }

  /**
   * Check the type of a DataList.
   *
   * @param list DataList
   * @param type type
   */
  public void checkType(DataList list, Class<?> type) {
    if (list.getType() != type) {
      throw new IllegalArgumentException("Invalid type");
    }
  }

  /**
   * Add a field.
   *
   * @param name   field name
   * @param values values
   */
  public void addField(String name, double[] values) {
    addField(name, new DoubleArrayList(values));
  }

  /**
   * Add a field.
   *
   * @param name   field name
   * @param values values
   */
  public void addField(String name, long[] values) {
    addField(name, new LongArrayList(values));
  }

  /**
   * Add a field.
   *
   * @param name   field name
   * @param values values
   */
  public void addField(String name, DoubleList values) {
    types.put(name, double.class);
    data.put(name, new DataDoubleList(values));
    fieldOrders.add(name);
  }

  /**
   * Add a field.
   *
   * @param name   field name
   * @param values values
   */
  public void addField(String name, LongList values) {
    types.put(name, long.class);
    data.put(name, new DataLongList(values));
    fieldOrders.add(name);
  }

  /**
   * Add a field.
   *
   * @param name     field name
   * @param dataList DataList
   */
  public void addField(String name, DataList dataList) {
    types.put(name, dataList.getType());
    data.put(name, dataList);
    fieldOrders.add(name);
  }

  /**
   * Add a value to a field.
   *
   * @param name  field name
   * @param value value
   */
  public void addValue(String name, double value) {
    // if field doesnt exists, create it and add it to fieldOrders
    checkFrozen(name);
    if (!data.containsKey(name)) {
      data.put(name, new DataDoubleList());
      fieldOrders.add(name);
    }
    addDouble(name, value);
  }

  /**
   * Add a value to a field.
   *
   * @param name  field name
   * @param value value
   */
  public void addValue(String name, long value) {
    checkFrozen(name);
    if (!data.containsKey(name)) {
      data.put(name, new DataLongList());
      fieldOrders.add(name);
    }
    addLong(name, value);
  }

  /**
   * Create a window of the DataFrame.
   *
   * @param start start index
   * @param end   end index (exclusive)
   * @return a new DataFrame
   * @throws IllegalArgumentException if start or end are invalid
   */
  public DataFrame window(int start, int end) {
    if (start == end) {
      // empty window with all fields present
      var df = new DataFrame();
      for (String field : fieldOrders) {
        df.addField(field, new DoubleArrayList());
      }
      return df;
    }
    int size = size();
    if (start < 0 || end > size || start > end) {
      throw new IllegalArgumentException("Invalid window");
    }
    DataFrame df = new DataFrame();
    for (String field : fieldOrders) {
      df.addField(field, data.get(field).subList(start, end));
    }
    return df;
  }

  /**
   * Get a field.
   *
   * @param field field name
   * @param index index
   * @return value
   */
  public double getDouble(String field, int index) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    checkType(data.get(field), double.class);
    return ((DataDoubleList) data.get(field)).get(index);
  }

  /**
   * Get the array of values for a field.
   *
   * @param field field name
   * @return array of values
   */
  public double[] getDoubles(String field) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    checkType(data.get(field), double.class);
    return ((DataDoubleList) data.get(field)).toArray();
  }

  /**
   * Get a field.
   *
   * @param field field name
   * @param index index
   * @return value
   */
  public long getLong(String field, int index) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    checkType(data.get(field), long.class);
    return ((DataLongList) data.get(field)).get(index);
  }

  /**
   * Get the array of values for a field.
   *
   * @param field field name
   * @return array of values
   */
  public long[] getLongs(String field) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    checkType(data.get(field), long.class);
    return ((DataLongList) data.get(field)).toArray();
  }

  /**
   *
   *
   * @return The smallest size of all fields.
   */
  public int size() {
    // return the size of the least populated field
    return data.values().stream().mapToInt(DataList::size).min().orElse(0);
  }

  /**
   * Save to CSV.
   *
   * @param fileName file name
   */
  public void saveToCSV(String fileName) {
    CSVUtils.writeCSV(this, fileName);
  }

  /**
   * Get a field as a string.
   * Used for CSV output.
   *
   * @param fieldName field name
   * @param index     index
   * @return value as a string
   */
  public String getAsString(String fieldName, int index) {
    if (!data.containsKey(fieldName))
      throw new IllegalArgumentException("Field " + fieldName +
          " does not exist");
    if (data.get(fieldName).getType() == double.class) {
      var d = getDouble(fieldName, index);
      if (Double.isNaN(d))
        return "";
      return Double.toString(d);
    } else if (data.get(fieldName).getType() == long.class) {
      var l = getLong(fieldName, index);
      if (l == Long.MIN_VALUE)
        return "";
      return Long.toString(l);
    } else {
      throw new IllegalArgumentException("Invalid type");
    }
  }

  private void addLong(String field, long value) {
    if (!data.containsKey(field)) {
      data.put(field, new DataLongList());
      fieldOrders.add(field);
    }
    checkType(data.get(field), long.class);
    ((DataLongList) data.get(field)).add(value);
  }

  private void addDouble(String field, double value) {
    if (!data.containsKey(field)) {
      data.put(field, new DataDoubleList());
      fieldOrders.add(field);
    }
    checkType(data.get(field), double.class);
    ((DataDoubleList) data.get(field)).add(value);
  }

  public HistoricalData toHistoricalData(CryptoPair pair) {
    return DataClasses.fromDataFrame(pair, this);
  }
}
