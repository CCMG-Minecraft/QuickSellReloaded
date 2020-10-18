package me.mrCookieSlime.QuickSell.boosters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BoosterMenu {

  /**
   * Show a booster overview to a player.
   *
   * @param player The player to show the booster overview of (themselves) to.
   */
  public static void showBoosterOverview(Player player) {
    ChestMenu menu = new ChestMenu("&3Booster Overview");

    addBoosterHeading(player, menu);
    menu.addMenuClickHandler(7, (p, slot, item, action) -> {
      showBoosterDetails(p, BoosterType.EXP);
      return false;
    });

    menu.open(player);
  }

  /**
   * Show details of a booster to a player.
   *
   * @param player The player to show details to.
   * @param type   The type of booster to to get information on
   */
  public static void showBoosterDetails(Player player, BoosterType type) {
    ChestMenu menu = new ChestMenu("&3" + StringUtils.format(type.toString()) + " Boosters");

    addBoosterHeading(player, menu);
    menu.addMenuClickHandler(7, (p, slot, item, action) -> {
      showBoosterDetails(p, BoosterType.EXP);
      return false;
    });

    int index = 9;

    for (Booster booster : Booster.getBoosters(player.getName(), type)) {
      menu.addItem(index, getBoosterItem(booster));
      menu.addMenuClickHandler(index, (plr, slot, stack, action) -> false);

      index++;
    }

    menu.open(player);
  }

  private static void addBoosterHeading(Player player, ChestMenu menu) {
    menu.addItem(1, new CustomItem(Material.GOLD_INGOT, "&bBoosters (Money)",
        "&7Current Multiplier: &b" + Booster.getMultiplier(player.getName(), BoosterType.MONETARY),
        "",
        "&7⇨ Click for Details"));
    menu.addMenuClickHandler(1, (p, slot, item, action) -> {
      showBoosterDetails(p, BoosterType.MONETARY);
      return false;
    });

    menu.addItem(7, new CustomItem(Material.EXPERIENCE_BOTTLE, "&bBoosters (Experience)",
        "&7Current Multiplier: &b" + Booster.getMultiplier(player.getName(), BoosterType.EXP), "",
        "&7⇨ Click for Details"));
  }

  /**
   * Get the Booster Item for menus for a specific booster.
   *
   * @param booster The booster to get the item for
   * @return The booster item
   */
  public static ItemStack getBoosterItem(Booster booster) {
    List<String> lore = new ArrayList<>();
    lore.add("");
    lore.add("&7Multiplier: &e" + booster.getBoosterMultiplier() + "x");
    lore.add("&7Time Left: &e" + (booster.isInfinite() ? "Infinite" : booster.formatTime() + "m"));
    lore.add("&7Global: " + (booster.isPrivate() ? "&4&l✘" : "&2&l✔"));
    lore.add("");
    lore.add("&7Contributors:");
    for (Map.Entry<String, Integer> entry : booster.getContributors().entrySet()) {
      lore.add(" &8⇨ " + entry.getKey() + ": &a+" + entry.getValue() + "m");
    }
    return new CustomItem(Material.EXPERIENCE_BOTTLE,
        "&3" + booster.getBoosterMultiplier() + "x &b" + booster.getUniqueName(),
        lore.toArray(new String[0]));
  }

  /**
   * Get the TellRaw message to send to a player.
   *
   * @param booster The booster
   * @return The TellRaw message
   */
  public static String getTellRawMessage(Booster booster) {
    StringBuilder builder = new StringBuilder(
        "&3" + booster.getBoosterMultiplier() + "x &b" + booster.getUniqueName() + "\n \n");
    builder
        .append("&7Multiplier: &e")
        .append(booster.getBoosterMultiplier())
        .append("x\n")
        .append("&7Time Left: &e")
        .append(booster.isInfinite() ? "Infinite" : booster.formatTime() + "m").append("\n")
        .append("&7Global: ")
        .append(booster.isPrivate() ? "&4&l✘" : "&2&l✔")
        .append("\n\n&7Contributors:\n");
    for (Map.Entry<String, Integer> entry : booster.getContributors().entrySet()) {
      builder
          .append(" &8⇨ ")
          .append(entry.getKey())
          .append(": &a+")
          .append(entry.getValue())
          .append("m\n");
    }

    return ChatColor.translateAlternateColorCodes('&', builder.toString());
  }

}
