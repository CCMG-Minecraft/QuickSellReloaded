package me.mrCookieSlime.QuickSell.commands.QSCommand;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import org.bukkit.entity.Player;

@CommandAlias("quicksell")
public class EditorCommand extends QSBaseCommand {

  @Subcommand("editor")
  @Description("Open the Editor")
  @CommandPermission("quicksell.editor|quicksell.admin")
  public void editorCommand(Player player) {
    QuickSell.getInstance().getEditor().openEditor(player);
  }

}
