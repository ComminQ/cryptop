package net.cryptop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import it.unimi.dsi.fastutil.longs.LongArrayList;

public class NumberJVMTest {

  @Test
  public void test_LongMinValue() {
    long val = Long.MIN_VALUE;
    String str = val == Long.MIN_VALUE ? "" : Long.toString(val);
    assertEquals("", str, "Long.MIN_VALUE is not equal to \"\"");
  }

  @Test
  public void test_LongMinValueInList(){
    var list = new LongArrayList();
    list.add(Long.MIN_VALUE);
    assertEquals(1, list.size());
    long val = list.getLong(0);
    String str = val == Long.MIN_VALUE ? "" : Long.toString(val);
    assertEquals("", str, "Long.MIN_VALUE is not equal to \"\"");
  }
}
