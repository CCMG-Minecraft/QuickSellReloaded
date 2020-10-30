package me.mrCookieSlime.QuickSell.util;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtility {

  /**
   * Rename an ItemStack.
   *
   * @param item The ItemStack
   * @param name The new name for the item
   */
  public static void rename(ItemStack item, String name) {
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
    item.setItemMeta(meta);
  }

  public static void setLore(ItemStack item, String... lore) {
    setLore(item, Arrays.asList(lore));
  }

  /**
   * Set the lore of an item.
   *
   * @param item The ItemStack which is being modified
   * @param lore The lore to set the ItemStack to
   */
  public static void setLore(ItemStack item, List<String> lore) {
    ListIterator<String> iterator = lore.listIterator();

    // Replace & colour codes
    while (iterator.hasNext()) {
      String line = iterator.next();
      iterator.set(ChatColor.translateAlternateColorCodes('&', line));
    }

    ItemMeta meta = item.getItemMeta();
    meta.setLore(lore);

    item.setItemMeta(meta);
  }
}
