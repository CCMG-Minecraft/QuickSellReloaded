package me.mrCookieSlime.quicksell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.audio.Soundboard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent e) {
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

          QuickSell.local.sendTranslation(e.getPlayer(), "commands.shop-created", false,
              new Variable("%shop%", e.getMessage()));

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
          QuickSell.local.sendTranslation(e.getPlayer(), "editor.renamed-shop", false);

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
          QuickSell.local.sendTranslation(e.getPlayer(), "editor.permission-set-shop", false);

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
    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(
        p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

    for (int i = 0; i < 54; i++) {
      final Shop shop = Shop.list().size() > i ? Shop.list().get(i) : null;
      if (shop == null) {
        menu.addItem(i, new CustomItem(Material.GOLD_NUGGET, "&cNew Shop", "",
            "&rLeft Click: &7Create a new Shop"));
        menu.addMenuClickHandler(i, (p12, slot, item, action) -> {
          input.put(p12.getUniqueId(), new Input(InputType.NEW_SHOP, slot));
          QuickSell.local.sendTranslation(p12, "editor.create-shop", false);
          p12.closeInventory();
          return false;
        });
      } else {
        menu.addItem(i, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), shop.getName(), "",
            "&rLeft Click: &7Edit Shop", "&rRight Click: &7Edit Shop Contents",
            "&rShift + Right Click: &4Delete Shop"));
        menu.addMenuClickHandler(i, (p13, slot, item, action) -> {
          if (action.isRightClicked()) {
            if (action.isShiftClicked()) {
              List<String> list = new ArrayList<>();
              for (Shop shop1 : Shop.list()) {
                list.add(shop1.getId());
              }
              list.remove(shop.getId());
              QuickSell.cfg.setValue("list", list);
              QuickSell.cfg.save();
              quicksell.reload();
              openEditor(p13);
            } else {
              openShopContentEditor(p13, shop, 1);
            }
          } else {
            openShopEditor(p13, shop);
          }
          return false;
        });
      }
    }

    menu.open(p);
  }

  /**
   * Open a specific shop in the Shop Editor to a player.
   *
   * @param p    the player to open the shop to
   * @param shop the shop to show the player
   */
  public void openShopEditor(Player p, final Shop shop) {
    quicksell.reload();
    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(p1 -> p1
        .playSound(p1.getLocation(), Soundboard.getLegacySounds("BLOCK_NOTE_PLING", "NOTE_PLING"),
            1F, 1F));

    menu.addItem(0,
        new CustomItem(Material.NAME_TAG, shop.getName(), "", "&rClick: &7Change Name"));
    menu.addMenuClickHandler(0, (p12, slot, item, action) -> {
      input.put(p12.getUniqueId(), new Input(InputType.RENAME, shop));
      QuickSell.local.sendTranslation(p12, "editor.rename-shop", false);
      p12.closeInventory();
      return false;
    });

    menu.addItem(1, new CustomItem(shop.getItem(ShopStatus.UNLOCKED), "&rDisplay Item", "",
        "&rClick: &7Change Item to the Item held in your Hand"));
    menu.addMenuClickHandler(1, (p13, slot, item, action) -> {
      p13.getInventory().getItemInMainHand();
      p13.getInventory().getItemInMainHand().getType();
      if (p13.getInventory().getItemInMainHand().getType() != Material.AIR) {
        //noinspection deprecation
        QuickSell.cfg.setValue("shops." + shop.getId() + ".itemtype",
            p13.getInventory().getItemInMainHand().getType().toString() + "-" + Objects
                .requireNonNull(p13.getInventory().getItemInMainHand().getData())
                .getData());
        QuickSell.cfg.save();
        quicksell.reload();
      }
      openShopEditor(p13, Shop.getShop(shop.getId()));
      return false;
    });

    menu.addItem(2, new CustomItem(Material.DIAMOND,
        "&7Shop Permission: &r" + (shop.getPermission().equals("") ? "None" : shop.getPermission()),
        "", "&rClick: &7Change Permission Node"));
    menu.addMenuClickHandler(2, (p14, slot, item, action) -> {
      input.put(p14.getUniqueId(), new Input(InputType.SET_PERMISSION, shop));
      QuickSell.local.sendTranslation(p14, "editor.set-permission-shop", false);
      p14.closeInventory();
      return false;
    });

    menu.addItem(3, new CustomItem(Material.COMMAND_BLOCK, "&bInheritance Manager", "",
        "&rClick: &7Open Inheritance Manager"));
    menu.addMenuClickHandler(3, (p15, slot, item, action) -> {
      openShopInheritanceEditor(p15, shop);
      return false;
    });

    menu.open(p);
  }

  /**
   * Open a Shop's Content Editor.
   *
   * @param p    the player to show the menu to
   * @param shop the shop to open
   * @param page the page of the menu to open
   */
  public void openShopContentEditor(Player p, final Shop shop, final int page) {
    quicksell.reload();
    ChestMenu menu = new ChestMenu("&6QuickSell - Shop Editor");

    menu.addMenuOpeningHandler(
        p1 -> p1.playSound(p1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));

    int index = 9;
    final int pages = shop.getPrices().getInfo().size() / shopSize + 1;

    for (int i = 0; i < 4; i++) {
      menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
      menu.addMenuClickHandler(i, (player, slot, item, action) -> false);
    }

    menu.addItem(4, new CustomItem(Material.GOLD_INGOT, "&7⇦ Back"));
    menu.addMenuClickHandler(4, (player, slot, item, action) -> {
      openEditor(p);
      return false;
    });

    for (int i = 5; i < 9; i++) {
      menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
      menu.addMenuClickHandler(i, (player, slot, item, action) -> false);
    }

    for (int i = 45; i < 54; i++) {
      menu.addItem(i, new CustomItem(Material.GRAY_STAINED_GLASS_PANE, " "));
      menu.addMenuClickHandler(i, (player, slot, item, action) -> false);
    }

    menu.addItem(46, new CustomItem(Material.LIME_STAINED_GLASS_PANE, "&r⇦ Previous Page", "",
        "&7(" + page + " / " + pages + ")"));
    menu.addMenuClickHandler(46, (p12, arg1, arg2, arg3) -> {
      int next = page - 1;
      if (next < 1) {
        next = pages;
      }
      if (next != page) {
        openShopContentEditor(p12, shop, next);
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
        openShopContentEditor(p13, shop, next);
      }
      return false;
    });

    int shopIndex = shopSize * (page - 1);

    for (int i = 0; i < shopSize; i++) {
      int target = shopIndex + i;
      if (target >= shop.getPrices().getItems().size()) {
        menu.addItem(index, new CustomItem(Material.COMMAND_BLOCK, "&cAdd Item", "",
            "&rLeft Click: &7Add an Item to this Shop"));
        menu.addMenuClickHandler(index, (p14, arg1, arg2, arg3) -> {
          openItemEditor(p14, shop);
          return false;
        });
        break;
      } else {
        final String string = shop.getPrices().getItems().get(target);
        final ItemStack item = shop.getPrices().getItem(string);

        menu.addItem(index, new CustomItem(item.getType(), item.getItemMeta().getDisplayName(),
            "&7Price (1): &6&$" + DoubleHandler.getFancyDouble(shop.getPrices().getPrice(string)),
            "", "&rLeft Click: &7Edit Price",
            "&rShift + Right Click: &7Remove this Item from this Shop"));
        menu.addMenuClickHandler(index, (player, slot, stack, action) -> {
          if (action.isShiftClicked() && action.isRightClicked()) {
            QuickSell.cfg.setValue("shops." + shop.getId() + ".price." + string, 0.0D);
            QuickSell.cfg.save();
            quicksell.reload();
            openShopContentEditor(p, Shop.getShop(shop.getId()), 1);
          } else if (!action.isRightClicked()) {
            openPriceEditor(p, Shop.getShop(shop.getId()), item, string,
                shop.getPrices().getPrice(string));
          }
          return false;
        });

        index++;
      }

    }

    menu.open(p);
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
      openShopContentEditor(p, Shop.getShop(shop.getId()), 1);

      QuickSell.local.sendTranslation(p, "commands.price-set", false,
          new Variable("%item%", item.getType().toString()), new Variable("%shop%", shop.getName()),
          new Variable("%price%", "1.0"));
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
      openShopContentEditor(p, Shop.getShop(shop.getId()), 1);

      //noinspection deprecation
      QuickSell.local.sendTranslation(p, "commands.price-set", false,
          new Variable("%item%", item.getType().toString() + ":" + Objects
              .requireNonNull(item.getData()).getData()),
          new Variable("%shop%", shop.getName()), new Variable("%price%", "1.0"));
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
      openShopContentEditor(player, Shop.getShop(shop.getId()), 1);

      QuickSell.local.sendTranslation(player, "commands.price-set", false, new Variable("%item%",
              item.getType().toString() + " named " + item.getItemMeta().getDisplayName()),
          new Variable("%shop%", shop.getName()), new Variable("%price%", "1.0"));
      player.sendMessage(
          ChatColor.translateAlternateColorCodes('&', "&7&oYou can edit the Price afterwards."));
      return false;
    });

    menu.addItem(16, new CustomItem(Material.RED_WOOL, "&cCancel"));
    menu.addMenuClickHandler(16, (player, slot, stack, action) -> {
      openShopContentEditor(player, Shop.getShop(shop.getId()), 1);
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

      QuickSell.local
          .sendTranslation(p19, "commands.price-set", false, new Variable("%item%", string),
              new Variable("%shop%", shop.getName()),
              new Variable("%price%", DoubleHandler.getFancyDouble(worth)));
      openShopContentEditor(p19, Shop.getShop(shop.getId()), 1);
      return false;
    });
    menu.addItem(24, new CustomItem(Material.RED_WOOL, "&4Cancel"));
    menu.addMenuClickHandler(24, (p110, arg1, arg2, arg3) -> {
      openShopContentEditor(p110, Shop.getShop(shop.getId()), 1);
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
