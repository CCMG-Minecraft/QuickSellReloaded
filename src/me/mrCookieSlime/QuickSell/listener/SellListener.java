package me.mrCookieSlime.QuickSell.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.transactions.SellEvent.Type;
import me.mrCookieSlime.QuickSell.transactions.SellProfile;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.shop.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SellListener implements Listener {

  public SellListener(QuickSell plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  /**
   * Handle Sell sign creations.
   *
   * @param e the bukkit sign create event
   */
  @EventHandler
  public void onSignCreate(SignChangeEvent e) {
    String prefix = ChatColor
        .translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix"));
    if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {
      if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
        e.setLine(0, prefix);
      } else {
        e.setCancelled(true);
        QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-permission", false);
      }
    }
    prefix = ChatColor
        .translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sellall-sign-prefix"));
    if (e.getLines()[0].equalsIgnoreCase(ChatColor.stripColor(prefix))) {
      if (e.getPlayer().hasPermission("QuickSell.sign.create")) {
        e.setLine(0, prefix);
      } else {
        e.setCancelled(true);
        QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-permission", false);
      }
    }
  }

  /**
   * Handle Player interactions with sell signs.
   *
   * @param e The bukkit sign click event
   */
  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (Objects.requireNonNull(e.getClickedBlock()).getState() instanceof Sign) {
        Sign sign = (Sign) e.getClickedBlock().getState();

        if (sign.getLine(0).equalsIgnoreCase(ChatColor
            .translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
          Shop shop = Shop.getShop(sign.getLine(1));
          if (shop != null) {
            ShopMenu.open(e.getPlayer(), shop);
          } else {
            ShopMenu.openMenu(e.getPlayer());
          }
          e.setCancelled(true);
        } else if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&',
            QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
          Shop shop = Shop.getShop(sign.getLine(1));
          if (shop != null) {
            if (shop.hasUnlocked(e.getPlayer())) {
              shop.sellAll(e.getPlayer(), Type.SELLALL);
            } else {
              QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-access", false);
            }
          } else if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop(e.getPlayer()) != null) {
              Objects.requireNonNull(Shop.getHighestShop(e.getPlayer()))
                  .sellAll(e.getPlayer(), Type.SELLALL);
            } else {
              QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-access", false);
            }
          } else {
            QuickSell.locale.sendTranslation(e.getPlayer(), "messages.unknown-shop", false);
          }
          e.setCancelled(true);
        }
      }
    } else if (e.getAction() == Action.LEFT_CLICK_BLOCK
        && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
      if (Objects.requireNonNull(e.getClickedBlock()).getState() instanceof Sign) {
        Sign sign = (Sign) e.getClickedBlock().getState();

        if (sign.getLine(0).equalsIgnoreCase(ChatColor
            .translateAlternateColorCodes('&', QuickSell.cfg.getString("options.sign-prefix")))) {
          Shop shop = Shop.getShop(sign.getLine(1));
          if (shop != null) {
            shop.showPrices(e.getPlayer());
          } else if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop(e.getPlayer()) != null) {
              Objects.requireNonNull(Shop.getHighestShop(e.getPlayer())).showPrices(e.getPlayer());
            } else {
              QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-access", false);
            }
          }
        } else if (sign.getLine(0).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&',
            QuickSell.cfg.getString("options.sellall-sign-prefix")))) {
          Shop shop = Shop.getShop(sign.getLine(1));
          if (shop != null) {
            if (shop.hasUnlocked(e.getPlayer())) {
              shop.showPrices(e.getPlayer());
            } else {
              QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-access", false);
            }
          } else if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop(e.getPlayer()) != null) {
              Objects.requireNonNull(Shop.getHighestShop(e.getPlayer())).showPrices(e.getPlayer());
            } else {
              QuickSell.locale.sendTranslation(e.getPlayer(), "messages.no-access", false);
            }
          } else {
            QuickSell.locale.sendTranslation(e.getPlayer(), "messages.unknown-shop", false);
          }
          e.setCancelled(true);
        }
      }
    }
  }

  /**
   * On Inventory Close.
   *
   * @param e the bukkit inventory close event
   */
  @EventHandler
  public void onClose(InventoryCloseEvent e) {
    Player p = (Player) e.getPlayer();
    if (QuickSell.shop.containsKey(p.getUniqueId())) {
      List<ItemStack> items = new ArrayList<>();
      int size = e.getInventory().getSize();
      if (QuickSell.cfg.getBoolean("options.enable-menu-line")) {
        size = size - 9;
      }
      for (int i = 0; i < size; i++) {
        items.add(e.getInventory().getContents()[i]);
      }
      Shop shop = QuickSell.shop.get(p.getUniqueId());
      QuickSell.shop.remove(p.getUniqueId());
      shop.sell(p, false, Type.SELL, items.toArray(new ItemStack[0]));
    }
  }

  /**
   * Inventory Click Event.
   *
   * @param e the bukkit inventory click event
   */
  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (QuickSell.cfg.getBoolean("options.enable-menu-line") && e.getRawSlot() < e.getInventory()
        .getSize()) {
      Player player = (Player) e.getWhoClicked();
      if (QuickSell.shop.containsKey(player.getUniqueId())) {
        Shop shop = QuickSell.shop.get(player.getUniqueId());

        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 9) {
          e.setCancelled(true);
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 8) {
          e.setCancelled(true);
          player.closeInventory();
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 7) {
          e.setCancelled(true);
        }

        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 6) {
          e.setCancelled(true);
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 5) {
          e.setCancelled(true);
          double money = 0.0;
          for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
            ItemStack item = e.getInventory().getContents()[i];
            money = money + shop.getPrices().getPrice(item);
          }

          money = Shop.fixDouble(money, 2);

          if (money > 0.0) {
            for (Booster booster : Booster.getBoosters(player.getName(), BoosterType.MONETARY)) {
              money = money + money * (booster.getBoosterMultiplier() - 1);
            }
          }
          QuickSell.locale.sendTranslation(
              player,
              "messages.estimate",
              false,
              "{MONEY}", String.valueOf(Shop.fixDouble(money, 2))
          );
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 4) {
          e.setCancelled(true);
        }

        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 3) {
          e.setCancelled(true);
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 2) {
          e.setCancelled(true);
          QuickSell.shop.remove(player.getUniqueId());
          for (int i = 0; i < e.getInventory().getSize() - 9; i++) {
            ItemStack item = e.getInventory().getContents()[i];
            if (item.getType() != Material.AIR) {
              if (player.getInventory().firstEmpty() >= 0) {
                player.getInventory().addItem(item);
              } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
              }
            }
          }
          player.closeInventory();
        }
        if (e.getSlot() == 9 * QuickSell.cfg.getInt("options.sell-gui-rows") - 1) {
          e.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    SellProfile.getProfile(e.getPlayer()).unregister();
  }
}
