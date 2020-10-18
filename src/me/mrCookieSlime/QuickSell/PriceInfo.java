package me.mrCookieSlime.QuickSell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PriceInfo {

  private static final Map<String, PriceInfo> map = new HashMap<>();
  Shop shop;
  Map<String, Double> prices;
  Map<String, ItemStack> info;
  List<String> order;
  int amount;

  /**
   * PriceInfo serves to give information about the price of a shop and its items.
   *
   * @param shop The shop to give price information for
   */
  public PriceInfo(Shop shop) {
    this.shop = shop;
    this.prices = new HashMap<>();
    this.order = new ArrayList<>();
    this.amount = QuickSell.cfg.getInt("shops." + shop.getId() + ".amount");

    for (String key : Objects.requireNonNull(QuickSell.cfg.getConfiguration()
        .getConfigurationSection("shops." + shop.getId() + ".price")).getKeys(false)) {
      if (!prices.containsKey(key)
          && QuickSell.cfg.getDouble("shops." + shop.getId() + ".price." + key) > 0.0) {
        prices
            .put(key, QuickSell.cfg.getDouble("shops." + shop.getId() + ".price." + key) / amount);
      }
    }

    for (String parent : QuickSell.cfg.getStringList("shops." + shop.getId() + ".inheritance")) {
      loadParent(parent);
    }

    info = new HashMap<>();
    for (String item : prices.keySet()) {
      if (info.size() >= 54) {
        break;
      }
      if (Material.getMaterial(item) != null) {
        info.put(item, new CustomItem(Objects.requireNonNull(Material.getMaterial(item)),
            "&r" + StringUtils.formatItemName(new ItemStack(
                Objects.requireNonNull(Material.getMaterial(item))), false), "",
            "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)),
            "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
        order.add(item);
      } else if (item.split("-").length > 1) {
        if (Material.getMaterial(item.split("-")[0]) != null) {
          if (!item.split("-")[1].equals("nodata")) {
            info.put(item, new CustomItem(
                new CustomItem(Objects.requireNonNull(Material.getMaterial(item.split("-")[0])),
                    item.split("-")[1], "",
                    "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)),
                    "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)),
                getAmount()));
          } else {
            info.put(item, new CustomItem(
                Objects.requireNonNull(Material.getMaterial(item.split("-")[0])),
                "&r" + StringUtils
                    .formatItemName(new ItemStack(
                        Objects.requireNonNull(Material.getMaterial(item.split("-")[0]))), false),
                "", "&7Worth (1): &6" + DoubleHandler.getFancyDouble(getPrices().get(item)),
                "&7Worth (64): &6" + DoubleHandler.getFancyDouble(getPrices().get(item) * 64)));
          }
          order.add(item);
        } else {
          System.err.println("[QuickSell] Could not recognize Item String: \"" + item + "\"");
        }
      } else {
        System.err.println("[QuickSell] Could not recognize Item String: \"" + item + "\"");
      }
    }

    map.put(shop.getId(), this);
  }

  /**
   * PriceInfo serves to give information about the price of a shop and its items.
   *
   * @param shop The shop to get price information for.
   */
  public PriceInfo(String shop) {
    this.prices = new HashMap<>();

    for (String key : Objects.requireNonNull(QuickSell.cfg.getConfiguration()
        .getConfigurationSection("shops." + shop + ".price")).getKeys(false)) {
      if (!prices.containsKey(key)
          && QuickSell.cfg.getDouble("shops." + shop + ".price." + key) > 0.0) {
        prices.put(key, QuickSell.cfg.getDouble("shops." + shop + ".price." + key) / amount);
      }
    }

    for (String parent : QuickSell.cfg.getStringList("shops." + shop + ".inheritance")) {
      for (String key : getInfo(parent).getPrices().keySet()) {
        if (!prices.containsKey(key)
            && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0) {
          prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
        }
      }
    }
  }

  public static PriceInfo getInfo(String shop) {
    return map.containsKey(shop) ? map.get(shop) : new PriceInfo(shop);
  }

  public Collection<ItemStack> getInfo() {
    return info.values();
  }

  private void loadParent(String parent) {
    for (String key : QuickSell.cfg.getKeys("shops." + parent + ".price")) {
      if (!prices.containsKey(key)
          && QuickSell.cfg.getDouble("shops." + parent + ".price." + key) > 0.0) {
        prices.put(key, QuickSell.cfg.getDouble("shops." + parent + ".price." + key) / amount);
      }
    }
    for (String p : QuickSell.cfg.getStringList("shops." + parent + ".inheritance")) {
      loadParent(p);
    }
  }

  public Map<String, Double> getPrices() {
    return prices;
  }

  /**
   * Get the Price for a specific ItemStack in the shop.
   *
   * @param item The Item to get the price of
   * @return 0 if invalid item, the price if valid item
   */
  public double getPrice(ItemStack item) {
    if (item == null) {
      return 0.0;
    }
    String string = toString(item);

    if (prices.containsKey(string)) {
      return DoubleHandler.fixDouble(prices.get(string) * item.getAmount());
    } else {
      return 0.0D;
    }
  }

  public double getPrice(String string) {
    return prices.get(string);
  }

  /**
   * Convert an ItemStack to a String. (why?)
   *
   * @param item The ItemStack to convert
   * @return The resultant string
   */
  public String toString(ItemStack item) {
    if (item == null) {
      return "null";
    }
    String name = item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta()
        .getDisplayName() : "" : "";
    if (!name.equalsIgnoreCase("") && prices.containsKey(item.getType().toString() + "-" + name)) {
      return item.getType().toString() + "-" + name;
    } else if (item.isSimilar(new ItemStack(item.getType(), item.getAmount())) && prices
        .containsKey(item.getType().toString() + "-nodata")) {
      return item.getType().toString() + "-nodata";
    } else if (prices.containsKey(item.getType().toString())) {
      return item.getType().toString();
    }
    return "null";
  }

  public int getAmount() {
    return amount;
  }

  public List<String> getItems() {
    return this.order;
  }

  public ItemStack getItem(String string) {
    return this.info.get(string);
  }
}
