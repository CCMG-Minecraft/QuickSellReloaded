package me.mrCookieSlime.QuickSell.transactions;

import org.bukkit.entity.Player;

public interface SellEvent {

  void onSell(Player p, Type type, int itemsSold, double money);

  @SuppressWarnings({"unused", "RedundantSuppression"})
  enum Type {

    SELL,
    SELLALL,
    AUTOSELL,
    CITIZENS,
    UNKNOWN

  }
}
