package me.mrCookieSlime.QuickSell.commands.QSCommand;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.QuickSell.QuickSell;
import me.mrCookieSlime.QuickSell.Shop;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;

@CommandAlias("quicksell")
public class NpcLinkCommands extends QSBaseCommand {

  /**
   * Link a selected NPC to a Shop.
   *
   * @param player The player executing the command
   * @param shop   The shop the NPC should be linked to
   * @param mode   The mode (sell/sellall)
   */
  @Subcommand("linknpc")
  @Description("Link a Citizens NPC to a Shop")
  @CommandPermission("quicksell.linknpc|quicksell.admin")
  @CommandCompletion("@nothing sell|sellall")
  @Syntax("<shop> <mode>")
  public void commandLinkNpc(Player player, String shop, String mode) {
    QuickSell instance = QuickSell.getInstance();

    if (citizensCheck(player)) {
      return;
    }

    NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
    Shop targetShop = Shop.getShop(shop);

    if (npc == null) {
      msg(
          player,
          "&cYou do not have an NPC selected. Run '/npc select' to select the NPC beside you."
      );
    } else if (targetShop == null) {
      QuickSell.locale.sendTranslation(player, "messages.unknown-shop", false);
    } else if (mode.equalsIgnoreCase("sell") || mode.equalsIgnoreCase("sellall")) {

      instance.getNpcs().setValue(String.valueOf(npc.getId()), shop + " ; " + mode.toUpperCase());
      instance.getNpcs().save();

      msg(
          player,
          String.format(
              "&a&lSuccess! &7The NPC &f%s &7is now a &f%s&7 NPC for shop &f%s&7.",
              npc.getName(),
              StringUtils.format(mode),
              targetShop.getName()
          )
      );

    }
  }

  /**
   * Un-Link a selected NPC from a Shop.
   *
   * @param player The player executing the command
   */
  @Subcommand("unlinknpc")
  @Description("Un-Link a Citizens NPC from a Shop")
  @CommandPermission("quicksell.unlinknpc|quicksell.admin")
  public void commandUnlinkNpc(Player player) {
    QuickSell instance = QuickSell.getInstance();

    if (citizensCheck(player)) {
      return;
    }

    NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(player);
    if (npc == null) {
      msg(
          player,
          "&cYou do not have an NPC selected. Run '/npc select' to select the NPC beside you."
      );
    } else if (instance.getNpcs().contains(String.valueOf(npc.getId()))) {
      instance.getNpcs().setValue(String.valueOf(npc.getId()), null);
      instance.getNpcs().save();
      msg(player, String.format(
          "&a&lSuccess! &7You have un-linked &f%s&7 from any QuickSell shop.",
          npc.getName()
      ));
    } else {
      msg(player, String.format("&c&lError.&7 &f%s&7 is not linked to any shop.", npc.getName()));
    }
  }

  /**
   * Check if Citizens is installed. Send an error message to the player provided if it is not.
   *
   * @param player The player to send the error message to
   * @return Whether or not Citizens is installed
   */
  private boolean citizensCheck(Player player) {
    if (!QuickSell.getInstance().isCitizensInstalled()) {
      msg(player, "&cYou need the &lCitizens&r&c plugin to run this command");
      return true;
    }

    return false;
  }

}
