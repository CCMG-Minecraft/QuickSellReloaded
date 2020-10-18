package me.mrCookieSlime.QuickSell.commands;

import co.aikar.commands.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class QSBaseCommand extends BaseCommand {

  /**
   * Send a message to a sender.
   *
   * @param sender  The command sender
   * @param message The message to send
   */
  public void msg(CommandSender sender, String message) {
    sender.sendMessage(
        ChatColor.translateAlternateColorCodes('&', message)
    );
  }

}
