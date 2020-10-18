package me.mrCookieSlime.QuickSell.transactions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.transactions.SellEvent.Type;
import org.bukkit.entity.Player;

public class SellProfile {

  public static Map<UUID, SellProfile> profiles = new HashMap<>();

  UUID uuid;
  Config cfg;
  List<String> transactions;

  /**
   * The Sell Profile is a profile of transactions made by a player, and is stored in persistent
   * data.
   *
   * @param player The player who the sell profile is of
   */
  public SellProfile(Player player) {
    uuid = player.getUniqueId();
    transactions = new ArrayList<>();
    cfg = new Config(
        new File(QuickSell.getInstance().getDataFolder(), "/transactions/" + player.getUniqueId() + ".log")
    );
    profiles.put(uuid, this);

    if (QuickSell.cfg.getBoolean("shop.enable-logging")) {
      for (String transaction : cfg.getKeys()) {
        transactions.add(cfg.getString(transaction));
      }
    }
  }

  public static SellProfile getProfile(Player p) {
    return profiles.containsKey(p.getUniqueId()) ? profiles.get(p.getUniqueId())
        : new SellProfile(p);
  }

  public void unregister() {
    save();
    profiles.remove(uuid);
  }

  public void save() {
    cfg.save();
  }

  /**
   * Store a transaction in persistent storage.
   *
   * @param type      The transaction type
   * @param soldItems The amount of items which were sold
   * @param money     The total value of all items sold
   */
  public void storeTransaction(Type type, int soldItems, double money) {
    long timestamp = System.currentTimeMillis();
    String string = String.format("%s__%s__%s__%s", timestamp, type, soldItems, money);
    cfg.setValue(String.valueOf(timestamp), string);
    transactions.add(string);
  }

}
