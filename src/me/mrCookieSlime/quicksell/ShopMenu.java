package me.mrCookieSlime.quicksell;

import java.util.Objects;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopMenu {

  static final int shopSize = 45;

  /**
   * Open a Shop Menu to a player.
   *
   * @param p    The player to open the shop to
   * @param shop The shop which should be opened
   */
  public static void open(Player p, Shop shop) {
    if (shop.hasUnlocked(p)) {
      Inventory inv = Bukkit
          .createInventory(null, 9 * QuickSell.cfg.getInt("options.sell-gui-rows"), ChatColor
              .translateAlternateColorCodes('&',
                  QuickSell.locale.getTranslation("menu.title").get(0)));
      if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 9,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 8,
            new CustomItem(Material.LIME_STAINED_GLASS_PANE,
                QuickSell.locale.getTranslation("menu.accept").get(0)));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 7,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 6,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 5,
            new CustomItem(Material.YELLOW_STAINED_GLASS_PANE,
                QuickSell.locale.getTranslation("menu.estimate").get(0)));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 4,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));

        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 3,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 2,
            new CustomItem(Material.RED_STAINED_GLASS_PANE,
                QuickSell.locale.getTranslation("menu.cancel").get(0)));
        inv.setItem(9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 1,
            new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
      }
      p.openInventory(inv);
      QuickSell.shop.put(p.getUniqueId(), shop);
    } else {
      QuickSell.locale.sendTranslation(p, "messages.no-access", false);
    }
  }

  /**
   * Open the generic 'shop selection' menu to a player.
   *
   * @param p The player to open the menu to
   */
  public static void openMenu(Player p) {
    if (QuickSell.cfg.getBoolean("shop.enable-hierarchy")) {
      if (Shop.getHighestShop(p) != null) {
        open(p, Objects.requireNonNull(Shop.getHighestShop(p)));
      } else {
        QuickSell.locale.sendTranslation(p, "messages.no-access", false);
      }
    } else {
      ChestMenu menu = new ChestMenu(QuickSell.locale.getTranslation("menu.title").get(0));

      for (int i = 0; i < Shop.list().size(); i++) {
        if (Shop.list().get(i) != null) {
          final Shop shop = Shop.list().get(i);
          menu.addItem(i,
              shop.getItem(shop.hasUnlocked(p) ? ShopStatus.UNLOCKED : ShopStatus.LOCKED));
          menu.addMenuClickHandler(i, (p1, slot, item, action) -> {
            ShopMenu.open(p1, shop);
            return false;
          });
        }
      }
      menu.open(p);
    }
  }

  /**
   * Open the Shop Prices menu to a player.
   *
   * @param p    The player to open the menu to
   * @param shop The shop which should be opened
   * @param page The page of the shop to open to the player
   */
  public static void openPrices(Player p, final Shop shop, final int page) {
    ChestMenu menu = new ChestMenu("Shop Prices");

    menu.addMenuOpeningHandler(p1 -> p1.playSound(p1.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F));

    int index = 0;
    final int pages = shop.getPrices().getInfo().size() / shopSize + 1;

    for (int i = 45; i < 54; i++) {
      menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
      menu.addMenuClickHandler(i, (arg0, arg1, arg2, arg3) -> false);
    }

    menu.addItem(46, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&r⇦ Previous Page", "",
        "&7(" + page + " / " + pages + ")"));
    menu.addMenuClickHandler(46, (p12, arg1, arg2, arg3) -> {
      int next = page - 1;
      if (next < 1) {
        next = pages;
      }
      if (next != page) {
        openPrices(p12, shop, next);
      }
      return false;
    });

    menu.addItem(52, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&rNext Page ⇨", "",
        "&7(" + page + " / " + pages + ")"));
    menu.addMenuClickHandler(52, (p13, arg1, arg2, arg3) -> {
      int next = page + 1;
      if (next > pages) {
        next = 1;
      }
      if (next != page) {
        openPrices(p13, shop, next);
      }
      return false;
    });

    int shopIndex = shopSize * (page - 1);

    for (int i = 0; i < shopSize; i++) {
      int target = shopIndex + i;
      if (target >= shop.getPrices().getItems().size()) {
        break;
      } else {
        final String string = shop.getPrices().getItems().get(target);
        final ItemStack item = shop.getPrices().getItem(string);
        menu.addItem(index, item);
        menu.addMenuClickHandler(index, (p14, arg1, arg2, action) -> false);
        index++;
      }

    }

    // TODO: Change this to InventoryFramework
    menu.build().open(p);
  }

}
