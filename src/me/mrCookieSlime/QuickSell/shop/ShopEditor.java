package me.mrCookieSlime.QuickSell.shop;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.configuration.Config;
import me.mrCookieSlime.QuickSell.input.Input;
import me.mrCookieSlime.QuickSell.input.InputType;
import me.mrCookieSlime.QuickSell.util.ItemUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ShopEditor implements Listener {

  final int shopSize = 36;
  Map<UUID, Input> input;
  QuickSell quicksell;

  /**
   * The Shop Editor is responsible for allowing server operators to edit shops via an in-game
   * editor.
   *
   * @param quicksell The plugin instance.
   */
  public ShopEditor(QuickSell quicksell) {
    this.quicksell = quicksell;
    this.input = new HashMap<>();
    quicksell.getServer().getPluginManager().registerEvents(this, quicksell);
  }

  /**
   * The chat listener is for any in-game shop editing, for instance when naming a shop.
   *
   * @param e the bukkit chat listener
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onChat(@SuppressWarnings("deprecation") PlayerChatEvent e) {
    if (input.containsKey(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);

      Input input = this.input.get(e.getPlayer().getUniqueId());

      switch (input.getType()) {
        case NEW_SHOP: {
          List<String> list = new ArrayList<>();
          for (Shop shop : Shop.list()) {
            list.add(shop.getId());
          }

          for (int i = list.size(); i <= (Integer) input.getValue(); i++) {
            list.add("");
          }

          list.set((Integer) input.getValue(),
              ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage())));

          QuickSell.cfg.setValue("list", list);
          QuickSell.cfg.setValue("shops." + ChatColor
                  .stripColor(ChatColor.translateAlternateColorCodes('&', e.getMessage()))
                  + ".name",
              e.getMessage());
          QuickSell.cfg.save();

          QuickSell.locale.sendTranslation(e.getPlayer(), "commands.shop-created", false, "%shop%",
              e.getMessage());

          openEditor(e.getPlayer());

          this.input.remove(e.getPlayer().getUniqueId());
          break;
        }
        case RENAME: {
          Shop shop = (Shop) input.getValue();

          QuickSell.cfg.setValue("shops." + shop.getId() + ".name", e.getMessage());
          QuickSell.cfg.save();
          quicksell.reload();

          openShopEditor(e.getPlayer(), Shop.getShop(shop.getId()));
          QuickSell.locale.sendTranslation(e.getPlayer(), "editor.renamed-shop", false);

          this.input.remove(e.getPlayer().getUniqueId());
          break;
        }
        case SET_PERMISSION: {
          Shop shop = (Shop) input.getValue();

          QuickSell.cfg.setValue("shops." + shop.getId() + ".permission",
              e.getMessage().equals("none") ? "" : e.getMessage());
          QuickSell.cfg.save();
          quicksell.reload();

          openShopEditor(e.getPlayer(), Shop.getShop(shop.getId()));
          QuickSell.locale.sendTranslation(e.getPlayer(), "editor.permission-set-shop", false);

          this.input.remove(e.getPlayer().getUniqueId());
          break;
        }
        default:
          break;
      }
    }
  }

  /**
   * Open the QuickSell shop editor to a server operator.
   *
   * @param p the player to show the menu to
   */
  public void openEditor(Player p) {
    quicksell.reload();
    Gui gui = new Gui(6, "Shop Editor");
    OutlinePane shops = new OutlinePane(0, 0, 9, 6);

    gui.setOnGlobalClick(e -> e.setCancelled(true));
    gui.addPane(shops);

    ItemStack newShopIcon = new ItemStack(Material.GOLD_NUGGET);
    ItemUtility.rename(newShopIcon, "&cEmpty Slot");
    ItemUtility.setLore(
        newShopIcon,
        "",
        "&7Click: &bCreate a New Shop"
    );

    for (int i = 0; i < 54; i++) {
      final Shop shop = Shop.list().size() > i ? Shop.list().get(i) : null;
      if (shop == null) {
        GuiItem item = new GuiItem(newShopIcon);
        item.setAction((event) -> {
          input.put(
              event.getWhoClicked().getUniqueId(), new Input(InputType.NEW_SHOP, event.getSlot())
          );
          QuickSell.getLocale().sendTranslation(event.getWhoClicked(), "editor.create-shop", false);
          event.getWhoClicked().closeInventory();
        });

        shops.addItem(item);
      } else {
        ItemStack icon = shop.getItem(ShopStatus.UNLOCKED);
        if (icon == null) {
          icon = new ItemStack(Material.GOLD_INGOT);
        }
        ItemUtility.rename(icon, String.format("&b%s", shop.getName()));
        ItemUtility.setLore(
            icon,
            "",
            "&7Left Click: &3Edit Shop Configuration",
            "&7Right Click: &3Edit Shop Items",
            "&7Shift + Right Click: &cDelete Shop"
        );

        GuiItem item = new GuiItem(icon);
        item.setAction((event) -> {
          if (event.isRightClick()) {
            if (event.isShiftClick()) {
              // Delete the Shop
              List<String> newShops = new ArrayList<>();
              for (Shop newShop : Shop.list()) {
                if (!newShop.getId().equals(shop.getId())) {
                  newShops.add(newShop.getId());
                }
              }
              QuickSell.getQuickSellConfig().setValue("list", newShops);
              QuickSell.getQuickSellConfig().save();
              quicksell.reload();

              openEditor((Player) event.getWhoClicked());
            } else {
              openShopContentEditor((Player) event.getWhoClicked(), shop);
            }
          } else {
            openShopEditor((Player) event.getWhoClicked(), shop);
          }
        });
        shops.addItem(item);
      }
    }

    gui.show(p);
  }

  /**
   * Open a specific shop in the Shop Editor to a player.
   *
   * @param player the player to open the shop to
   * @param shop   the shop to show the player
   */
  public void openShopEditor(Player player, final Shop shop) {
    quicksell.reload();
    Gui gui = new Gui(1, String.format("Shop Configuration: %s", shop.getId()));
    OutlinePane pane = new OutlinePane(0, 0, 9, 1);

    gui.setOnGlobalClick(e -> e.setCancelled(true));
    gui.addPane(pane);

    ItemStack renameIcon = new ItemStack(Material.NAME_TAG);
    ItemUtility.rename(renameIcon, String.format("&7Current Name: &b%s", shop.getName()));
    ItemUtility.setLore(
        renameIcon,
        "",
        "&7Click to Change Name"
    );
    GuiItem rename = new GuiItem(renameIcon);
    rename.setAction((event) -> {
      input.put(event.getWhoClicked().getUniqueId(), new Input(InputType.RENAME, shop));
      event.getWhoClicked().closeInventory();
    });
    pane.addItem(rename);

    ItemStack displayIcon = shop.getItem(ShopStatus.UNLOCKED);
    ItemUtility.rename(displayIcon, "&7Display Icon");
    ItemUtility.setLore(
        displayIcon,
        "",
        "&7Click to set to the item",
        "&7you are currently holding."
    );
    GuiItem display = new GuiItem(displayIcon);
    display.setAction((event) -> {
      event.getWhoClicked().closeInventory();
      ItemStack held = event.getWhoClicked().getInventory().getItemInMainHand();
      if (held.getType() != Material.AIR) {
        QuickSell.getQuickSellConfig().setValue(
            String.format("shops.%s.itemtype", shop.getId()),
            String.format("%s-nodata", held.getType().name())
        );
        QuickSell.getQuickSellConfig().save();
        quicksell.reload();
      }
    });
    pane.addItem(display);

    ItemStack permissionIcon = new ItemStack(Material.DIAMOND);
    ItemUtility.rename(permissionIcon, String.format("&7Permission:&b %s", shop.getPermission()));
    ItemUtility.setLore(
        permissionIcon,
        "",
        "&7Click to Change Permission"
    );
    GuiItem permission = new GuiItem(permissionIcon);
    permission.setAction((event) -> {
      input.put(event.getWhoClicked().getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
      QuickSell.getLocale()
          .sendTranslation(event.getWhoClicked(), "editor.set-permission-shop", false);
      event.getWhoClicked().closeInventory();
    });
    pane.addItem(permission);

    ItemStack inheritanceIcon = new ItemStack(Material.COMMAND_BLOCK);
    ItemUtility.rename(inheritanceIcon, "&7Inheritance Manager");
    ItemUtility.setLore(
        inheritanceIcon,
        "",
        "&7Click to Open the Inheritance",
        "&7Manager."
    );
    GuiItem inheritance = new GuiItem(inheritanceIcon);
    inheritance.setAction(e -> openShopInheritanceEditor((Player) e.getWhoClicked(), shop));
    pane.addItem(inheritance);

    gui.show(player);
  }

  /**
   * Open a Shop's Content Editor.
   *
   * @param player the player to show the menu to
   * @param shop   the shop to open
   */
  public void openShopContentEditor(Player player, final Shop shop) {
    quicksell.reload();

    ItemStack backButtonIcon = new ItemStack(Material.GOLD_INGOT);
    ItemUtility.rename(backButtonIcon, "&7⇦ Back");
    GuiItem backButton = new GuiItem(backButtonIcon);
    backButton.setAction(e -> openEditor(player));

    ItemStack blackGlassIcon = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemUtility.rename(blackGlassIcon, " ");
    GuiItem blackGlass = new GuiItem(blackGlassIcon);

    Gui gui = new Gui(6, String.format("Content Editor: %s", shop.getId()));
    gui.setOnGlobalClick(e -> e.setCancelled(true));

    StaticPane header = new StaticPane(0, 0, 9, 1);
    gui.addPane(header);
    for (int i = 0; i < 9; i++) {
      if (i == 4) {
        header.addItem(backButton, i, 0);
      } else {
        header.addItem(blackGlass, i, 0);
      }
    }

    PaginatedPane contents = new PaginatedPane(0, 1, 9, 4);
    gui.addPane(contents);
    Map<Integer, OutlinePane> pages = new HashMap<>();

    int currentPage = 0;
    int currentSlot = 36;

    for (String key : shop.getPrices().getItems()) {
      currentSlot++;
      if (currentSlot > 36) {
        currentSlot = 0;
        currentPage++;

        pages.put(currentPage, new OutlinePane(0, 0, 9, 4));
      }

      ItemStack icon = shop.getPrices().getItem(key);
      ItemUtility.setLore(
          icon,
          String.format("&7Price (1): &6$%s", shop.getPrices().getPrice(key)),
          String.format("&7Price (64): &6$%s", shop.getPrices().getPrice(key) * 64),
          "",
          "&7Left Click: &bEdit Price",
          "&7Shift & Right Click: &cRemove Item"
      );
      GuiItem item = new GuiItem(icon);
      item.setAction(e -> {
        if (e.isRightClick() && e.isShiftClick()) {
          Config config = QuickSell.getQuickSellConfig();
          config.setValue(String.format("shops.%s.price.%s", shop.getId(), key), 0.0D);
          config.save();
          quicksell.reload();
          openShopContentEditor(player, shop);
        } else if (e.isLeftClick()) {
          openPriceEditor(
              player,
              shop,
              shop.getPrices().getItem(key),
              key,
              shop.getPrices().getPrice(key)
          );
        }
      });

      pages.get(currentPage).addItem(item);
    }

    for (Entry<Integer, OutlinePane> entry : pages.entrySet()) {
      contents.addPane(entry.getKey(), entry.getValue());
    }

    StaticPane footer = new StaticPane(0, 5, 9, 1);
    gui.addPane(footer);

    ItemStack backIcon = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    ItemUtility.rename(backIcon, "&7⇦ Previous Page");
    GuiItem back = new GuiItem(backIcon);
    back.setAction(event -> {
      if (contents.getPage() != 1) {
        contents.setPage(contents.getPage() - 1);
        gui.update();
      }
    });

    ItemStack addItemIcon = new ItemStack(Material.COMMAND_BLOCK);
    ItemUtility.rename(addItemIcon, "&bAdd Item");
    ItemUtility.setLore(
        addItemIcon,
        "",
        "&7Add the item you are currently",
        "&7holding to this shop."
    );
    GuiItem addItem = new GuiItem(addItemIcon);
    addItem.setAction(event -> openItemEditor(player, shop));

    ItemStack nextIcon = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    ItemUtility.rename(nextIcon, "&7⇦ Previous Page");
    GuiItem next = new GuiItem(nextIcon);
    next.setAction(event -> {
      if (contents.getPage() != contents.getPages()) {
        contents.setPage(contents.getPage() + 1);
        gui.update();
      }
    });

    for (int i = 0; i < 9; i++) {
      if (i == 1) {
        footer.addItem(back, i, 0);
      } else if (i == 4) {
        footer.addItem(addItem, i, 0);
      } else if (i == 7) {
        footer.addItem(next, i, 0);
      } else {
        footer.addItem(blackGlass, i, 0);
      }
    }

    contents.setPage(1);
    gui.show(player);
  }

  /**
   * Open the Item Editor for a player.
   *
   * @param p    The player to show the item editor to
   * @param shop The Shop that's being edited
   */
  public void openItemEditor(Player p, final Shop shop) {
    final ItemStack item = p.getInventory().getItemInMainHand();

    if (item.getType() == Material.AIR) {
      p.sendMessage(ChatColor.translateAlternateColorCodes('&',
          "&4&lYou need to be holding the Item you want to add in your Hand!"));
      return;
    }

    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(
        (player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

    menu.addItem(4, item);
    menu.addMenuClickHandler(4, (player, slot, stack, action) -> false);

    menu.addItem(10, new CustomItem(Material.LIME_WOOL, "&2Material Only &7(e.g. STONE)",
        "&rAdds the Item above to the Shop", "&rThis Option is going to ignore",
        "&rany Item Names and such"));
    menu.addMenuClickHandler(10, (player, slot, stack, action) -> {
      QuickSell.cfg.setValue("shops." + shop.getId() + ".price." + item.getType().toString(), 1.0D);
      QuickSell.cfg.save();
      quicksell.reload();
      openShopContentEditor(p, Shop.getShop(shop.getId()));

      QuickSell.locale.sendTranslation(p, "commands.price-set", false,
          "%item%", item.getType().toString(),
          "%shop%", shop.getName(),
          "%price%", "1.0");
      p.sendMessage(
          ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
      return false;
    });

    menu.addItem(11, new CustomItem(Material.LIME_WOOL,
        "&2Material Only and exclude Metadata &7(e.g. STONE-nodata)",
        "&rAdds the Item above to the Shop", "&rThis Option is going to only take Items",
        "&rwhich are NOT renamed and do NOT have Lore"));
    menu.addMenuClickHandler(11, (player, slot, stack, action) -> {
      QuickSell.cfg
          .setValue("shops." + shop.getId() + ".price." + item.getType().toString() + "-nodata",
              1.0D);
      QuickSell.cfg.save();
      quicksell.reload();
      openShopContentEditor(p, Shop.getShop(shop.getId()));

      //noinspection deprecation
      QuickSell.locale.sendTranslation(p, "commands.price-set", false,
          "%item%",
          item.getType().toString() + ":" + Objects.requireNonNull(item.getData()).getData(),
          "%shop%", shop.getName(),
          "%price%", "1.0"
      );
      p.sendMessage(
          ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
      return false;
    });

    menu.addItem(12, new CustomItem(Material.CYAN_WOOL,
        "&2Material + Display Name &7(e.g. STONE named &5Cool Stone &7)",
        "&rAdds the Item above to the Shop", "&rThis Option is going to respect Display Names"));
    menu.addMenuClickHandler(14, (player, slot, stack, action) -> {
      if (!item.getItemMeta().hasDisplayName()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&cYou can only choose this Option if the selected Item had a Display Name!"));
        return false;
      }

      QuickSell.cfg.setValue(
          "shops." + shop.getId() + ".price." + item.getType().toString() + "-" + item.getItemMeta()
              .getDisplayName().replaceAll("&", "&"), 1.0D);
      QuickSell.cfg.save();
      quicksell.reload();
      openShopContentEditor(player, Shop.getShop(shop.getId()));

      QuickSell.locale.sendTranslation(
          player,
          "commands.price-set",
          false,
          "%item%", item.getType().toString() + " named " + item.getItemMeta().getDisplayName(),
          "%shop%", shop.getName(),
          "%price%", "1.0");
      player.sendMessage(
          ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
      return false;
    });

    menu.addItem(16, new CustomItem(Material.RED_WOOL, "&cCancel"));
    menu.addMenuClickHandler(16, (player, slot, stack, action) -> {
      openShopContentEditor(player, Shop.getShop(shop.getId()));
      return false;
    });

    menu.open(p);
  }

  /**
   * Open the editor that sets the price of an item in a QuickSell shop.
   *
   * @param p      The player to open the editor to
   * @param shop   The shop which should be opened
   * @param item   The item which is being set in the shop
   * @param string (I have literally no idea what this is used for)
   * @param worth  The current cost of the item in the shop
   */
  public void openPriceEditor(Player p, final Shop shop, final ItemStack item, final String string,
      final double worth) {
    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(
        (player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

    menu.addItem(4, new CustomItem(item, item.getItemMeta().getDisplayName(), "",
        "&8Price: &6$" + DoubleHandler.getFancyDouble(worth)));
    menu.addMenuClickHandler(4, (player, slot, stack, action) -> false);

    menu.addItem(9,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+0.1", "&7Shift + Left Click: &r+1", "&7Right Click: &r-0.1",
            "&7Shift + Right Click: &r-1"));
    menu.addMenuClickHandler(9, (player, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 1 : 0.1);
      } else {
        price = price + (action.isShiftClicked() ? 1 : 0.1);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p, shop, item, string, price);
      return false;
    });

    menu.addItem(10,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+10", "&7Shift + Left Click: &r+100", "&7Right Click: &r-10",
            "&7Shift + Right Click: &r-100"));
    menu.addMenuClickHandler(10, (p1, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 100 : 10);
      } else {
        price = price + (action.isShiftClicked() ? 100 : 10);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p1, shop, item, string, price);
      return false;
    });

    menu.addItem(11,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+1K", "&7Shift + Left Click: &r+10K", "&7Right Click: &r-1K",
            "&7Shift + Right Click: &r-10K"));
    menu.addMenuClickHandler(11, (p12, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 10000 : 1000);
      } else {
        price = price + (action.isShiftClicked() ? 10000 : 1000);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p12, shop, item, string, price);
      return false;
    });

    menu.addItem(12,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+100K", "&7Shift + Left Click: &r+1M", "&7Right Click: &r-100K",
            "&7Shift + Right Click: &r-1M"));
    menu.addMenuClickHandler(12, (p13, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 1000000 : 100000);
      } else {
        price = price + (action.isShiftClicked() ? 1000000 : 100000);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p13, shop, item, string, price);
      return false;
    });

    menu.addItem(13,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+10M", "&7Shift + Left Click: &r+100M", "&7Right Click: &r-10M",
            "&7Shift + Right Click: &r-100M"));
    menu.addMenuClickHandler(13, (p14, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 100000000 : 10000000);
      } else {
        price = price + (action.isShiftClicked() ? 100000000 : 10000000);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p14, shop, item, string, price);
      return false;
    });

    menu.addItem(14,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+1B", "&7Shift + Left Click: &r+10B", "&7Right Click: &r-1B",
            "&7Shift + Right Click: &r-10B"));
    menu.addMenuClickHandler(14, (p15, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 10000000000D : 1000000000);
      } else {
        price = price + (action.isShiftClicked() ? 10000000000D : 1000000000);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p15, shop, item, string, price);
      return false;
    });

    menu.addItem(15,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+100B", "&7Shift + Left Click: &r+1T", "&7Right Click: &r-100B",
            "&7Shift + Right Click: &r-1T"));
    menu.addMenuClickHandler(15, (p16, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 1000000000000D : 100000000000D);
      } else {
        price = price + (action.isShiftClicked() ? 1000000000000D : 100000000000D);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p16, shop, item, string, price);
      return false;
    });

    menu.addItem(16,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+10T", "&7Shift + Left Click: &r+100T", "&7Right Click: &r-10T",
            "&7Shift + Right Click: &r-100T"));
    menu.addMenuClickHandler(16, (p17, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 100000000000000D : 10000000000000D);
      } else {
        price = price + (action.isShiftClicked() ? 100000000000000D : 10000000000000D);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p17, shop, item, string, price);
      return false;
    });

    menu.addItem(17,
        new CustomItem(Material.GOLD_INGOT, "&7Price: &6$" + DoubleHandler.getFancyDouble(worth),
            "", "&7Left Click: &r+1Q", "&7Shift + Left Click: &r+10Q", "&7Right Click: &r-1Q",
            "&7Shift + Right Click: &r-10Q"));
    menu.addMenuClickHandler(17, (p18, slot, stack, action) -> {
      double price = worth;
      if (action.isRightClicked()) {
        price = price - (action.isShiftClicked() ? 10000000000000000D : 1000000000000000D);
      } else {
        price = price + (action.isShiftClicked() ? 10000000000000000D : 1000000000000000D);
      }
      if (price <= 0) {
        price = 0.1;
      }
      openPriceEditor(p18, shop, item, string, price);
      return false;
    });

    menu.addItem(20, new CustomItem(Material.LIME_WOOL, "&2Save"));
    menu.addMenuClickHandler(20, (p19, arg1, arg2, arg3) -> {
      QuickSell.cfg.setValue("shops." + shop.getId() + ".price." + string, worth);
      QuickSell.cfg.save();
      quicksell.reload();

      QuickSell.locale.sendTranslation(
          p19,
          "commands.price-set",
          false,
          "%item%", string,
          "%shop%", shop.getName(),
          "%price%", DoubleHandler.getFancyDouble(worth));
      openShopContentEditor(p19, Shop.getShop(shop.getId()));
      return false;
    });
    menu.addItem(24, new CustomItem(Material.RED_WOOL, "&4Cancel"));
    menu.addMenuClickHandler(24, (p110, arg1, arg2, arg3) -> {
      openShopContentEditor(p110, Shop.getShop(shop.getId()));
      return false;
    });

    menu.open(p);
  }

  /**
   * Open a shop's inheritance editor.
   *
   * @param targetPlayer The player to show the editor to
   * @param theShop      The shop to open the editor of
   */
  public void openShopInheritanceEditor(final Player targetPlayer, final Shop theShop) {
    quicksell.reload();
    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(
        (player) -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

    for (int i = 0; i < 54; i++) {
      if (Shop.list().size() <= i) {
        break;
      }
      final Shop shop = Shop.list().get(i);

      if (!shop.getId().equalsIgnoreCase(theShop.getId())) {
        final boolean inherit = QuickSell.cfg
            .getStringList("shops." + theShop.getId() + ".inheritance")
            .contains(shop.getId());

        menu.addItem(i, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), shop.getName(), "",
            "&7Inherit: " + (inherit ? "&2&l✔" : "&4&l✘"), "", "&7&oClick to toggle"));
        menu.addMenuClickHandler(i, (player, slot, item, action) -> {
          List<String> shops = QuickSell.cfg
              .getStringList("shops." + theShop.getId() + ".inheritance");
          if (inherit) {
            shops.remove(shop.getId());
          } else {
            shops.add(shop.getId());
          }
          QuickSell.cfg.setValue("shops." + theShop.getId() + ".inheritance", shops);
          QuickSell.cfg.save();
          openShopInheritanceEditor(targetPlayer, Shop.getShop(theShop.getId()));
          return false;
        });
      }
    }

    menu.open(targetPlayer);
  }

}
