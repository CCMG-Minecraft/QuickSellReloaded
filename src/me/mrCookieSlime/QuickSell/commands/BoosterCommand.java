package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandAlias("booster")
public class BoosterCommand extends QSBaseCommand {

  /**
   * Active a booster.
   *
   * @param sender     The command sender
   * @param typeName   The booster type (all/monetary/exp)
   * @param player     The player who is responsible for the booster
   * @param multiplier The booster multiplier
   * @param duration   The booster duration in minutes
   */
  @Default
  @CommandPermission("quicksell.booster|quicksell.admin")
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
      if (type != null) {
        Booster booster = new Booster(type, player.getName(), multiplier, duration);
        booster.activate();
      } else {
        for (BoosterType bt : BoosterType.values()) {
          Booster booster = new Booster(bt, player.getName(), multiplier, duration);
          booster.activate();
        }
      }
    }
  }

}
