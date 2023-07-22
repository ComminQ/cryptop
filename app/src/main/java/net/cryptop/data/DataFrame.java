package net.cryptop.data;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
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
  private Map<String, DoubleList> data = new HashMap<>();

  /**
   * List of field names in order.
   */
  private List<String> fieldOrders = new ArrayList<>();

  /**
   * Add a field.
   *
   * @param name field name
   * @param values values
   */
  public void addField(String name, double[] values) {
    addField(name, new DoubleArrayList(values));
  }

  /**
   * Add a field.
   *
   * @param name field name
   * @param values values
   */
  public void addField(String name, DoubleList values) {
    data.put(name, values);
    fieldOrders.add(name);
  }

  /**
   * Add a value to a field.
   *
   * @param name field name
   * @param value value
   */
  public void addValue(String name, double value) {
    // if field doesnt exists, create it and add it to fieldOrders
    if (!data.containsKey(name)) {
      data.put(name, new DoubleArrayList());
      fieldOrders.add(name);
    }
    data.get(name).add(value);
  }

  /**
   * Create a window of the DataFrame.
   *
   * @param start start index
   * @param end end index (exclusive)
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
  public double get(String field, int index) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    return data.get(field).getDouble(index);
  }

  /**
   * Get the array of values for a field.
   *
   * @param field field name
   * @return array of values
   */
  public double[] get(String field) {
    if (!data.containsKey(field))
      throw new IllegalArgumentException("Field " + field + " does not exist");
    return data.get(field).toDoubleArray();
  }

  /**
   *
   *
   * @return The smallest size of all fields.
   */
  public int size() {
    // return the size of the least populated field
    return data.values().stream().mapToInt(DoubleList::size).min().orElse(0);
  }

  /**
   * Save to CSV.
   *
   * @param fileName file name
   */
  public void saveToCSV(String fileName) { CSVUtils.writeCSV(this, fileName); }
}
