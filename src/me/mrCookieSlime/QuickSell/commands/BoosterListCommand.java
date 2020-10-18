package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.mrCookieSlime.QuickSell.boosters.BoosterMenu;
import org.bukkit.entity.Player;

@CommandAlias("boosters")
public class BoosterListCommand extends QSBaseCommand {

  @Default
  @CommandPermission("quicksell.boosters|quicksell.admin")
  public void commandBooster(Player player) {
    BoosterMenu.showBoosterOverview(player);
  }

}
