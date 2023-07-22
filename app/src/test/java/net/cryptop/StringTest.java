package net.cryptop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StringTest {
  
  @Test
  public void test_Split(){
    String row = "0,0,,3";
    String[] values = row.split(",");
    assertEquals(4, values.length);
    assertEquals(0, Double.parseDouble(values[0]));
    assertEquals(0, Double.parseDouble(values[1]));
    assertEquals("", values[2]);
    assertEquals(3, Double.parseDouble(values[3]));
  }

}
