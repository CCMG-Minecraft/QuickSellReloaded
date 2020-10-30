package me.mrCookieSlime.QuickSell.boosters;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BoosterMenu {

  /**
   * Show a booster overview to a player.
   *
   * @param player The player to show the booster overview of (themselves) to.
   */
  public static void showBoosterOverview(Player player) {
    Gui gui = new Gui(1, "Booster Overview");
    gui.setOnGlobalClick(e -> e.setCancelled(true));

    StaticPane pane = new StaticPane(0, 0, 9, 1);
    gui.addPane(pane);

    addBoosterHeading(player, pane, false);

    gui.show(player);
  }

  /**
   * Show details of a booster to a player.
   *
   * @param player The player to show details to.
   * @param type   The type of booster to to get information on
   */
  public static void showBoosterDetails(Player player, BoosterType type) {
    Gui gui = new Gui(
        6,
        String.format("%s Boosters", StringUtils.capitalize(type.name().toLowerCase()))
    );
    gui.setOnGlobalClick(e -> e.setCancelled(true));

    StaticPane header = new StaticPane(0, 0, 9, 2);
    addBoosterHeading(player, header, true);
    gui.addPane(header);

    OutlinePane content = new OutlinePane(0, 2, 9, 4);

    for (Booster booster : Booster.getBoosters(player.getName(), type)) {
      content.addItem(new GuiItem(getBoosterItem(booster)));
    }

    gui.show(player);
  }

  private static void addBoosterHeading(Player player, StaticPane menu, Boolean secondRow) {
    ItemStack monetaryIcon = new ItemStack(Material.GOLD_INGOT);
    String monetaryName = "§bBoosters (Money)";
    double monetaryBooster = Booster.getMultiplier(player.getName(), BoosterType.MONETARY);
    List<String> monetaryLore = Arrays.asList(
        String.format("§7Current Multiplier: §b%s§7.", monetaryBooster),
        "",
        "§7⇨ Click for Details"
    );
    ItemMeta monetaryMeta = monetaryIcon.getItemMeta();
    monetaryMeta.setDisplayName(monetaryName);
    monetaryMeta.setLore(monetaryLore);
    monetaryIcon.setItemMeta(monetaryMeta);
    GuiItem monetary = new GuiItem(monetaryIcon);
    monetary.setAction((event) -> {
      event.setCancelled(true);
      showBoosterDetails(player, BoosterType.MONETARY);
    });

    ItemStack experienceIcon = new ItemStack(Material.EXPERIENCE_BOTTLE);
    String experienceName = "§bBoosters (Experience)";
    double experienceBooster = Booster.getMultiplier(player.getName(), BoosterType.EXP);
    List<String> experienceLore = Arrays.asList(
        String.format("§7Current Multiplier: §b%s§7.", experienceBooster),
        "",
        "§7⇨ Click for Details"
    );
    ItemMeta experienceMeta = experienceIcon.getItemMeta();
    experienceMeta.setDisplayName(experienceName);
    experienceMeta.setLore(experienceLore);
    experienceIcon.setItemMeta(experienceMeta);
    GuiItem experience = new GuiItem(experienceIcon);
    experience.setAction((event) -> {
      event.setCancelled(true);
      showBoosterDetails(player, BoosterType.EXP);
    });

    menu.addItem(monetary, 1, 0);
    menu.addItem(experience, 7, 0);

    if (secondRow) {
      ItemStack blankIcon = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
      ItemMeta blankMeta = blankIcon.getItemMeta();
      blankMeta.setDisplayName(" ");
      blankIcon.setItemMeta(blankMeta);
      GuiItem blank = new GuiItem(blankIcon);
      for (int i = 0; i < 9; i++) {
        menu.addItem(blank, i, 1);
      }
    }
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
