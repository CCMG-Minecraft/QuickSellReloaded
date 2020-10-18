package me.mrCookieSlime.QuickSell;

import me.mrCookieSlime.QuickSell.SellEvent.Type;

public class Transaction {

  long timestamp;
  int items;
  double money;
  Type type;

  /**
   * A transaction handles any exchange of items for money through QuickSell.
   *
   * @param timestamp The time that the items were sold
   * @param type      The way that the item was sold
   * @param soldItems The items that were sold to the server
   * @param money     The amount of money which should be received.
   */
  public Transaction(long timestamp, Type type, int soldItems, double money) {
    this.timestamp = timestamp;
    this.type = type;
    this.items = soldItems;
    this.money = money;
  }

  public static int getItemsSold(String string) {
    return Integer.parseInt(string.split(" __ ")[2]);
  }

  public static double getMoney(String string) {
    return Double.parseDouble(string.split(" __ ")[3]);
  }

}
