package me.mrCookieSlime.QuickSell.commands.qscommand;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import org.bukkit.command.CommandSender;

@CommandAlias("quicksell")
public class ReloadCommand extends QSBaseCommand {

  @Subcommand("reload")
  @Description("Reload the plugin")
  @CommandPermission("quicksell.reload|quicksell.admin")
  public void reloadCommand(CommandSender sender) {
    QuickSell.getInstance().reload();
    QuickSell.locale.sendTranslation(sender, "commands.reload.done", false);
  }

}
