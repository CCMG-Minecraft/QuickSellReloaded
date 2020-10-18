package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandAlias("pbooster|privatebooster")
public class PrivateBoosterCommand extends QSBaseCommand {

  /**
   * Active a private booster.
   *
   * @param sender     The command sender
   * @param typeName   The booster type (all/monetary/exp)
   * @param player     The player who the booster is for
   * @param multiplier The booster multiplier
   * @param duration   The booster duration in minutes
   */
  @Default
  @CommandPermission("quicksell.privatebooster|quicksell.admin")
  @Syntax("<type> <player> <multiplier> <duration>")
  @CommandCompletion("all|monetary|exp @players @nothing @nothing")
  public void commandBooster(
      CommandSender sender,
      String typeName,
      OfflinePlayer player,
      Double multiplier,
      Integer duration
  ) {

    BoosterType type = typeName.equalsIgnoreCase("all")
        ? null
        : BoosterType.valueOf(typeName.toUpperCase());

    if (type != null || typeName.equalsIgnoreCase("all")) {
      try {
        if (type != null) {
          PrivateBooster booster = new PrivateBooster(type, player.getName(), multiplier, duration);
          booster.activate();
        } else {
          for (BoosterType bt : BoosterType.values()) {
            PrivateBooster booster = new PrivateBooster(bt, player.getName(), multiplier, duration);
            booster.activate();
          }
        }
      } catch (NumberFormatException x) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&4Usage: &c/pbooster <all/monetary/prisongems/exp/mcmmo/casino> <Name of the "
                + "Player> <Multiplier> <Duration in Minutes>"));
      }
    } else {
      QuickSell.locale.sendTranslation(sender, "commands.booster.invalid-type", false);
    }
  }

}
