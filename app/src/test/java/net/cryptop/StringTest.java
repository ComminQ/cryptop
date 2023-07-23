package net.cryptop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StringTest {
  
  @Test
  public void test_Split(){
    String row = "0,0,,3";
    String[] values = row.split(",", -1);
    assertEquals(4, values.length);
    assertEquals(0, Double.parseDouble(values[0]));
    assertEquals(0, Double.parseDouble(values[1]));
    assertEquals("", values[2]);
    assertEquals(3, Double.parseDouble(values[3]));
  }

  @Test
  public void test_Split_Complex(){
    // date,open,high,low,close,volume,SMA10,SMA50,Savgol,NoiseSavgol,RSI7,RSI14
    String row = "1636531200000,66426.42,66857.03,66357.0,66612.15,1250.31052,,,,,26.33775453913084,,";
    String[] values = row.split(",", -1);
    assertEquals(13, values.length);
    assertEquals(1636531200000L, Long.parseLong(values[0]));
    assertEquals(66426.42, Double.parseDouble(values[1]));
    assertEquals(66857.03, Double.parseDouble(values[2]));
    assertEquals(66357.0, Double.parseDouble(values[3]));
    assertEquals(66612.15, Double.parseDouble(values[4]));
    assertEquals(1250.31052, Double.parseDouble(values[5]));
    assertEquals("", values[6]);
    assertEquals("", values[7]);
    assertEquals("", values[8]);
    assertEquals("", values[9]);
    assertEquals(26.33775453913084, Double.parseDouble(values[10]));
    assertEquals("", values[11]);
    assertEquals("", values[12]);
  }

}
