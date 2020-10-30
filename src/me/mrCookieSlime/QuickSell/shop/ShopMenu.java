package me.mrCookieSlime.QuickSell.shop;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.util.ItemUtility;
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
   * @param player The player to open the menu to
   */
  public static void openMenu(Player player) {
    if (QuickSell.cfg.getBoolean("shop.enable-hierarchy")) {
      if (Shop.getHighestShop(player) != null) {
        open(player, Objects.requireNonNull(Shop.getHighestShop(player)));
      } else {
        QuickSell.locale.sendTranslation(player, "messages.no-access", false);
      }
    } else {
      String name = QuickSell.locale.getTranslation("menu.title").get(0);
      name = ChatColor.translateAlternateColorCodes('&', name);
      Gui gui = new Gui(6, name);
      OutlinePane pane = new OutlinePane(0, 0, 9, 6);

      gui.setOnGlobalClick(e -> e.setCancelled(true));
      gui.addPane(pane);

      for (int i = 0; i < Shop.list().size(); i++) {
        if (Shop.list().get(i) != null) {
          final Shop shop = Shop.list().get(i);

          ItemStack icon = shop.getItem(shop.hasUnlocked(player) ? ShopStatus.UNLOCKED : ShopStatus.LOCKED);
          GuiItem item = new GuiItem(icon);

          item.setAction(event -> ShopMenu.open((Player) event.getWhoClicked(), shop));

          pane.addItem(item);
        }
      }
      gui.show(player);
    }
  }

  /**
   * Open the Shop Prices menu to a player.
   *
   * @param player The player to open the menu to
   * @param shop   The shop which should be opened
   */
  public static void openPrices(Player player, final Shop shop) {

    // Construct the GUI and its Panes
    Gui gui = new Gui(6, "Shop Prices");
    gui.setOnGlobalClick((event) -> event.setCancelled(true));

    PaginatedPane contents = new PaginatedPane(0, 0, 9, 5);
    gui.addPane(contents);

    StaticPane footer = new StaticPane(0, 5, 9, 1);
    gui.addPane(footer);

    final int pages = shop.getPrices().getInfo().size() / shopSize + 1;

    buildFooter(gui, contents, footer, pages);

    int inShop = 0;
    int currentPage = 1;

    Map<Integer, OutlinePane> pagePanes = new HashMap<>();
    pagePanes.put(currentPage, new OutlinePane(0, 0, 9, 5));

    for (int i = 0; i < shop.getPrices().getItems().size(); i++) {
      inShop++;
      if (inShop > 45) {
        inShop = 0;
        currentPage++;
        pagePanes.put(currentPage, new OutlinePane(0, 0, 9, 5));
      }

      final String string = shop.getPrices().getItems().get(i);
      final ItemStack item = shop.getPrices().getItem(string);
      final GuiItem guiItem = new GuiItem(item);
      pagePanes.get(currentPage).addItem(guiItem);
    }

    for (Entry<Integer, OutlinePane> entry : pagePanes.entrySet()) {
      contents.addPane(entry.getKey(), entry.getValue());
    }

    contents.setPage(1);
    gui.show(player);
  }

  /**
   * Build the GUI Footer.
   *
   * @param gui      The GUI Object
   * @param contents The Paginated Pane
   * @param footer   The Footer Pane
   * @param pages    The amount of Pages in the GUI
   */
  private static void buildFooter(
      Gui gui,
      PaginatedPane contents,
      StaticPane footer,
      Integer pages
  ) {

    ItemStack nextButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
    ItemUtility.rename(nextButton, "&rNext Page ⇨");
    GuiItem nextGuiItem = new GuiItem(nextButton);
    nextGuiItem.setAction((event) -> {
      if (contents.getPage() < pages) {
        ((Player) event.getWhoClicked()).playSound(
            event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f
        );
        contents.setPage(contents.getPage() + 1);
        gui.update();
      }
    });

    ItemStack backButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
    ItemUtility.rename(backButton, "&r⇦ Previous Page");
    GuiItem backGuiItem = new GuiItem(backButton);
    backGuiItem.setAction((event) -> {
      if (contents.getPage() != 0) {
        ((Player) event.getWhoClicked()).playSound(
            event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f
        );
        contents.setPage(contents.getPage() - 1);
        gui.update();
      }
    });

    ItemStack blankItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
    ItemUtility.rename(blankItem, " ");
    GuiItem blankGuiItem = new GuiItem(blankItem);

    // Add the footer of the GUI.
    for (int x = 0; x < 9; x++) {
      footer.addItem(blankGuiItem, x, 0);
    }

    footer.addItem(backGuiItem, 1, 0);
    footer.addItem(nextGuiItem, 7, 0);
  }

}
