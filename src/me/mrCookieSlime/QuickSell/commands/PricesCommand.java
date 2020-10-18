package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import java.util.Objects;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.shop.Shop;
import org.bukkit.entity.Player;

@CommandAlias("prices")
public class PricesCommand extends QSBaseCommand {

  /**
   * Open the Sell menu to a player if there's multiple shops, or open the highest shop they can.
   *
   * @param sender The command sender
   */
  @CommandPermission("quicksell.prices")
  @Default
  public void commandPrices(Player sender) {
    if (QuickSell.cfg.getBoolean("options.open-only-shop-with-permission")) {
      if (Shop.getHighestShop(sender) != null) {
        Objects.requireNonNull(Shop.getHighestShop(sender)).showPrices(sender);
      } else {
        QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
      }
    } else {
      QuickSell.locale.sendTranslation(sender, "commands.prices.usage", false);
    }
  }

  /**
   * Open a specific shop to a player.
   *
   * @param sender The command sender
   * @param shopId The Shop ID
   */
  @CommandPermission("quicksell.prices")
  @CommandCompletion("@availableshops")
  @Default
  public void commandSellAll(Player sender, String shopId) {
    Shop shop = Shop.getShop(shopId);
    if (shop != null) {
      if (shop.hasUnlocked(sender) || sender.hasPermission("quicksell.prices.anyshop")) {
        shop.showPrices(sender);
      } else {
        QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
      }
    } else {
      QuickSell.locale.sendTranslation(sender, "messages.unknown-shop", false);
    }
  }

}
