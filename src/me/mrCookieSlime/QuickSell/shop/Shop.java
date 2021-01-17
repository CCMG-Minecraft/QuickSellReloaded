package me.mrCookieSlime.QuickSell.shop;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.configuration.CustomItem;
import me.mrCookieSlime.QuickSell.configuration.Variable;
import me.mrCookieSlime.QuickSell.transactions.PriceInfo;
import me.mrCookieSlime.QuickSell.transactions.SellEvent;
import me.mrCookieSlime.QuickSell.transactions.SellEvent.Type;
import me.mrCookieSlime.QuickSell.util.ItemUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Shop {

  private static final List<Shop> shops = new ArrayList<>();
  private static final Map<String, Shop> map = new HashMap<>();

  String shop;
  String permission;
  PriceInfo prices;
  ItemStack unlocked;
  ItemStack locked;
  String name;

  /**
   * A shop is a server-owned marketplace, where players can trade in their items for monetary
   * rewards.
   *
   * @param id The Shop ID
   */
  public Shop(String id) {
    this.shop = id;

    try {
      this.prices = new PriceInfo(this);
    } catch (Exception exception) {
      // Ignored exception
    }

    name = QuickSell.cfg.getString("shops." + shop + ".name");
    permission = QuickSell.cfg.getString("shops." + shop + ".permission");

    List<String> lore = new ArrayList<>();
    lore.add("");
    lore.add(ChatColor.translateAlternateColorCodes('&', "&7<&a&l Click to open &7>"));
    for (String line : QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
      lore.add(ChatColor.translateAlternateColorCodes('&', line));
    }

    try {
      unlocked = new ItemStack(
          Objects.requireNonNull(
              Material.getMaterial(QuickSell.cfg.getString("shops." + shop + ".itemtype"))));
      ItemUtility.rename(unlocked, name);
      ItemUtility.setLore(unlocked, lore);
    } catch (NullPointerException e) {
      QuickSell.getInstance().getLogger().severe(String.format(
          "Not registering shop %s as it does not have 'itemtype' defined in its configuration.",
          shop
      ));
    }

    lore = new ArrayList<>();
    lore.add(ChatColor.translateAlternateColorCodes('&',
        QuickSell.locale.getTranslation("messages.no-access").get(0)));
    for (String line : QuickSell.cfg.getStringList("shops." + shop + ".lore")) {
      lore.add(ChatColor.translateAlternateColorCodes('&', line));
    }

    locked = new ItemStack(Objects
        .requireNonNull(Material.getMaterial(QuickSell.cfg.getString("options.locked-item"))));
    ItemUtility.rename(locked, name);
    ItemUtility.setLore(locked, lore);

    shops.add(this);
    map.put(this.shop.toLowerCase(), this);
  }

  public Shop() {
    shops.add(null);
  }

  public static void reset() {
    shops.clear();
    map.clear();
  }

  public static List<Shop> list() {
    return shops;
  }

  /**
   * Get the highest shop that the player currently has access to.
   *
   * @param player The player
   * @return The highest shop the player can access
   */
  public static Shop getHighestShop(Player player) {
    for (int i = shops.size() - 1; i >= 0; i--) {
      if (shops.get(i) != null && shops.get(i).hasUnlocked(player)) {
        return shops.get(i);
      }
    }
    return null;
  }

  public static Shop getShop(String id) {
    return map.get(id.toLowerCase());
  }

  public static String getFancyDouble(double d) {
    DecimalFormat format = new DecimalFormat("##.##");

    double d2 = d / 1000000000000000d;
    if (d2 > 1) {
      return format.format(d2).replace(",", ".") + "Q";
    }

    d2 = d / 1000000000000d;
    if (d2 > 1) {
      return format.format(d2).replace(",", ".") + "T";
    }

    d2 = d / 1000000000d;
    if (d2 > 1) {
      return format.format(d2).replace(",", ".") + "B";
    }

    d2 = d / 1000000d;
    if (d2 > 1) {
      return format.format(d2).replace(",", ".") + "M";
    }

    d2 = d / 1000d;
    if (d2 > 1) {
      return format.format(d2).replace(",", ".") + "K";
    }

    return format.format(d).replace(",", ".");
  }

  /**
   * Parse double configurations.
   *
   * @param amount The amount of digits
   * @param digits How many decimal places to format to.
   * @return The fixed double.
   */
  public static double fixDouble(double amount, int digits) {
    if (digits == 0) {
      return (int) amount;
    }
    StringBuilder format = new StringBuilder("##");
    for (int i = 0; i < digits; i++) {
      if (i == 0) {
        format.append(".");
      }
      format.append("#");
    }
    return Double
        .parseDouble(new DecimalFormat(format.toString()).format(amount).replace(",", "."));
  }

  /**
   * Check if a player has unlocked this shop.
   *
   * @param p The player to check
   * @return Whether or not the player can access this shop
   */
  public boolean hasUnlocked(Player p) {
    return permission.equalsIgnoreCase("") || p.hasPermission(permission);
  }

  public String getId() {
    return shop;
  }

  public String getPermission() {
    return permission;
  }

  public PriceInfo getPrices() {
    return prices;
  }

  /**
   * Sell all items in the player's inventory to the shop.
   *
   * @param player The player
   * @param type   The type of SellAll
   */
  public void sellAll(Player player, Type type) {
    sellAll(player, type, QuickSell.cfg.getBoolean("options.sellall-gui-prompt"));
  }

  /**
   * Sell all items in the player's inventory to the shop.
   *
   * @param player The player
   * @param type   The type of SellAll
   */
  public void sellAll(Player player, Type type, boolean prompt) {
    if (prompt) {
      String title = ChatColor.translateAlternateColorCodes(
          '&', QuickSell.locale.getTranslation("sellall.prompt.title").get(0)
      );
      Gui promptGui = new Gui(1, title);
      OutlinePane pane = new OutlinePane(0, 0, 9, 1);
      promptGui.addPane(pane);

      ItemStack button = new CustomItem(
          Material.LIME_STAINED_GLASS_PANE,
          QuickSell.locale.getTranslation("sellall.prompt.item.name").get(0),
          QuickSell.locale.getTranslation("sellall.prompt.item.lore").toArray(new String[0])
      );

      GuiItem buttonItem = new GuiItem(button, (inventoryClickEvent) -> {
        inventoryClickEvent.setCancelled(true);
        sellAll(player, type, false);
        inventoryClickEvent.getWhoClicked().closeInventory();
      });
      for (int i = 0; i < 9; i++) {
        pane.addItem(buttonItem);
      }

      promptGui.show(player);
    } else {
      List<ItemStack> items = new ArrayList<>();
      for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
        ItemStack is = player.getInventory().getItem(slot);
        if (getPrices().getPrice(is) > 0.0) {
          items.add(is);
          player.getInventory().setItem(slot, null);
        }
      }
      player.updateInventory();
      sell(player, false, type, items.toArray(new ItemStack[0]));
    }
  }

  /**
   * Sell an item[s] to a shop.
   *
   * @param player    The player selling the items
   * @param silent    Whether or not this is a silent sell
   * @param type      The type of Sell
   * @param soldItems All items which are being sold to the shop
   */
  public void sell(Player player, boolean silent, Type type, ItemStack... soldItems) {
    if (soldItems.length == 0) {
      if (!silent) {
        QuickSell.locale.sendTranslation(player, "messages.no-items", false);
      }
    } else {
      double money = 0.0;
      int sold = 0;
      int total = 0;

      for (ItemStack item : soldItems) {
        if (item != null) {
          total = total + item.getAmount();
          if (getPrices().getPrice(item) > 0.0) {
            sold = sold + item.getAmount();
            money = money + getPrices().getPrice(item);
          } else if (player.getInventory().firstEmpty() >= 0) {
            player.getInventory().addItem(item);
          } else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
          }
        }
      }

      money = fixDouble(money, 2);

      if (money > 0.0) {
        double totalmoney = handoutReward(player, money, sold, silent);
        if (!silent) {
          if (QuickSell.cfg.getBoolean("sound.enabled")) {
            player.playSound(player.getLocation(),
                Sound.valueOf(QuickSell.cfg.getString("sound.sound")), 1F,
                1F);
          }
          for (String command : QuickSell.cfg.getStringList("commands-on-sell")) {
            String cmd = command;
            if (cmd.contains("{PLAYER}")) {
              cmd = cmd.replace("{PLAYER}", player.getName());
            }
            if (cmd.contains("{MONEY}")) {
              cmd = cmd.replace("{MONEY}", String.valueOf(fixDouble(totalmoney, 2)));
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
          }
        }
        for (SellEvent event : QuickSell.getSellEvents()) {
          event.onSell(player, type, total, totalmoney);
        }
      } else if (!silent && total > 0) {
        QuickSell.locale.sendTranslation(player, "messages.get-nothing", false);
      }
      if (!silent && sold < total && total > 0) {
        QuickSell.locale.sendTranslation(player, "messages.dropped", false);
      }
    }
    player.updateInventory();
  }

  /**
   * Give the reward (money) to a player, with the message and any relevant booster applied.
   *
   * @param player     The player receiving reward money
   * @param totalMoney The total money which the player should receive (pre-booster)
   * @param items      How many items have been sold to the server
   * @param silent     Should the handout be silent (no notification to the player)?
   * @return The full amount which was given to the player.
   */
  public double handoutReward(Player player, double totalMoney, int items, boolean silent) {
    double money = totalMoney;
    if (!silent) {
      QuickSell.locale.sendTranslation(player, "messages.sell", false,
          "{MONEY}", getFancyDouble(money),
          "{ITEMS}", String.valueOf(items));
    }
    for (Booster booster : Booster.getBoosters(player.getName())) {
      if (booster.getType().equals(BoosterType.MONETARY)) {
        if (!silent) {
          booster.sendMessage(player, new Variable("{MONEY}",
              getFancyDouble(money * (booster.getBoosterMultiplier() - 1))));
        }
        money = money + money * (booster.getBoosterMultiplier() - 1);
      }
    }
    if (!silent && !Booster.getBoosters(player.getName()).isEmpty()) {
      QuickSell.locale.sendTranslation(
          player,
          "messages.total",
          false,
          "{MONEY}", getFancyDouble(money)
      );
    }
    money = fixDouble(money, 2);
    QuickSell.economy.depositPlayer(player, money);
    return money;
  }

  /**
   * Get the Item for a ShopStatus.
   *
   * @param status The shop's current status
   * @return The relevant item, or NULL if invalid status.
   */
  public ItemStack getItem(ShopStatus status) {
    ItemStack item;
    switch (status) {
      case LOCKED:
        item = locked;
        break;
      case UNLOCKED:
        item = unlocked;
        break;
      default:
        item = null;
    }

    if (item == null) {
      return new ItemStack(Material.GOLD_INGOT);
    }

    return item;
  }

  public String getName() {
    return ChatColor.translateAlternateColorCodes('&', name);
  }

  public void showPrices(Player p) {
    ShopMenu.openPrices(p, this);
  }

}
