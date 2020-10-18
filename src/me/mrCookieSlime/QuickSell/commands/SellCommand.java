package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.Objects;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.ShopMenu;
import org.bukkit.entity.Player;

@CommandAlias("sell")
public class SellCommand extends QSBaseCommand {

  /**
   * Open the Sell menu to a player if there's multiple shops, or open the highest shop they can.
   *
   * @param sender The command sender
   */
  @CommandPermission("quicksell.sell")
  @Default
  public void commandSell(Player sender) {
    if (Shop.list().size() == 1) {
      ShopMenu.open(sender, Shop.list().get(0));
    } else if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
      if (Shop.getHighestShop(sender) != null) {
        ShopMenu.open(
            sender,
            Objects.requireNonNull(Shop.getHighestShop(sender))
        );
      } else {
        QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
      }
    } else {
      ShopMenu.openMenu(sender);
    }
  }

  /**
   * Open a specific shop to a player.
   *
   * @param sender The command sender
   * @param shopId The Shop ID
   */
  @CommandPermission("quicksell.sell")
  @Default
  public void commandSell(Player sender, String shopId) {
    Shop shop = Shop.getShop(shopId);
    if (shop != null) {
      if (shop.hasUnlocked(sender)) {
        ShopMenu.open(sender, shop);
      } else {
        QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
      }
    } else {
      ShopMenu.openMenu(sender);
    }
  }

}
