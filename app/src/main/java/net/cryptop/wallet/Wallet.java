package net.cryptop.wallet;

import java.util.HashMap;
import java.util.Map;

public class Wallet implements Cloneable {

  public Map<String, Double> balances = new HashMap<>();

  /**
   * Set the balance of a crypto pair
   *
   * @param crypto The crypto (e.g. BTC / ETH, or USDT / BUSD for stable coins)
   * @param balance The balance
   */
  public void setBalance(String crypto, Double balance) {
    balances.put(crypto, balance);
  }

  /**
   * Get the balance of a crypto pair
   *
   * @param crypto The crypto (e.g. BTC / ETH, or USDT / BUSD for stable coins)
   * @return The balance
   */
  public Double getBalance(String crypto) {
    return balances.getOrDefault(crypto, 0.0);
  }

  @Override
  public Wallet clone() {
    Wallet wallet = new Wallet();
    wallet.balances = new HashMap<>(this.balances);
    return wallet;
  }
}
