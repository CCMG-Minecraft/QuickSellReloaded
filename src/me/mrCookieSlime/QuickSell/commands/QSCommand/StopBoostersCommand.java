package me.mrCookieSlime.QuickSell.commands.QSCommand;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import java.util.Iterator;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandAlias("quicksell")
public class StopBoostersCommand extends QSBaseCommand {

  /**
   * Stop any boosters applicable to a player.
   *
   * @param sender The command sender
   * @param player The player whom to stop boosters of
   */
  @Subcommand("stopboosters")
  @Description("Stop any boosters applicable to a player")
  @CommandPermission("quicksell.stopboosters|quicksell.admin")
  public void commandStopBoosters(CommandSender sender, OfflinePlayer player) {
    Iterator<Booster> boosters = Booster.iterate();
    while (boosters.hasNext()) {
      Booster booster = boosters.next();
      if (booster.getAppliedPlayers().contains(player.getName())) {
        boosters.remove();
        booster.deactivate();
      }
    }
    QuickSell.locale.sendTranslation(
        sender,
        "booster.reset",
        false,
        "%player%", player.getName());
  }

  /**
   * Stop any boosters running on the server.
   *
   * @param sender The command sender
   */
  @Subcommand("stopboosters")
  @Description("Stop any boosters running on the server")
  @CommandPermission("quicksell.stopboosters|quicksell.admin")
  public void commandStopBoosters(CommandSender sender) {
    Iterator<Booster> boosters = Booster.iterate();
    while (boosters.hasNext()) {
      Booster booster = boosters.next();
      boosters.remove();
      booster.deactivate();
    }
    QuickSell.locale.sendTranslation(sender, "boosters.reset", false);
  }

}
