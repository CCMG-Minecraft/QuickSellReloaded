package me.mrCookieSlime.QuickSell.commands.qscommand;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import java.util.HashMap;
import java.util.Map;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import org.bukkit.command.CommandSender;

@CommandAlias("quicksell")
public class MainCommand extends QSBaseCommand {

  /**
   * The help command serves to give information on commands to a player.
   *
   * @param sender The Command Sender
   */
  @Default
  @HelpCommand
  @Subcommand("help")
  @Description("Allows you to manage your Shops")
  @CommandPermission("quicksell.help")
  public void commandHelp(CommandSender sender) {
    Map<String, String> commands = new HashMap<>();
    commands.put("quicksell", "Shows this menu");
    commands.put("quicksell reload", "Reload QuickSell");
    commands.put("quicksell editor", "Open the in-game Shop Editor");
    commands.put("quicksell stopboosters [player]", "Stop Boosters");
    commands.put("quicksell linknpc <shop> <sell/sellall>", "Link a NPC to a Shop");
    commands.put("quicksell unlinknpc", "Un-link selected NPC from its shop");
    commands.put("sell [shop]", "Sell your items to a shop");
    commands.put("sellall <shop>", "Sell all your items to a shop");
    commands.put("prices <shop>", "Get the prices for a specific shop");
    commands.put("booster <type> <author> <multiplier> <duration>", "Activate a booster");
    commands.put("pbooster <type> <player> <multiplier> <duration>", "Give a player a booster.");

    QuickSell instance = QuickSell.getInstance();

    msg(sender, "");
    msg(sender, "&b&l QuickSell &r&9Reloaded &7(&f" + instance.getDescription().getVersion() + "&7)");
    msg(sender, "&8  Plugin Help");
    msg(sender, "");

    for (Map.Entry<String, String> command : commands.entrySet()) {
      msg(sender, String.format("&6/%s", command.getKey()));
      msg(sender, String.format("&7&o  %s", command.getValue()));
    }
  }

}
