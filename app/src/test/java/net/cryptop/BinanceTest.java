package net.cryptop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.cryptop.utils.binance.BinanceData;
import org.junit.jupiter.api.Test;

public class BinanceTest {

  @Test
  public void test_BinancePing() {
    assertTrue(BinanceData.ping().join());
  }

  @Test
  public void test_BinanceServerTime() {
    // ensure that delta between server time and local time is less than 1
    // second
    assertEquals(System.currentTimeMillis(),
                 BinanceData.serverTime().join(), 1000L);
  }
}
