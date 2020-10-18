package me.mrCookieSlime.QuickSell.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class Localization {

  private final Config config;

  /**
   * Creates a new Localization Object for the specified Plugin.
   *
   * @param plugin The Plugin this Localization is made for
   */
  public Localization(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "messages.yml");
    this.config = new Config(file);
  }

  /**
   * Sets the Default Message/s for the specified Key.
   *
   * @param key      The Key of those Messages
   * @param messages The Messages which this key will refer to by default
   */
  public void setDefault(String key, String... messages) {
    if (!config.contains(key)) {
      config.setValue(key, Arrays.asList(messages));
    }
  }

  /**
   * Returns the Strings referring to the specified Key.
   *
   * @param key The Key of those Messages
   * @return The List this key is referring to
   */
  public List<String> getTranslation(String key) {
    return config.getValue(key) instanceof String ? Collections.singletonList(config.getString(key))
        : config.getStringList(key);
  }

  /**
   * Sends all Messages the key is referring to including the Prefix and specified Variables.
   *
   * @param sender       The Player/Console who should receive these Messages
   * @param key          The Key of those Messages
   * @param addPrefix    Specify whether the Prefix will get added or not
   * @param placeholders All Variables which should be applied to the sent messages
   */
  public void sendTranslation(CommandSender sender, String key, boolean addPrefix,
      String... placeholders) {
    String prefix = addPrefix && config.contains("prefix") ? getTranslation("prefix").get(0) : "";
    for (String translation : getTranslation(key)) {
      try {
        for (int i = 0; i < placeholders.length; i += 2) {
          translation = translation.replace(placeholders[i], placeholders[i + 1]);
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        e.printStackTrace();
      }
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + translation));
    }
  }

  /**
   * Reloads the messages.yml File.
   */
  public void reload() {
    config.reload();
  }

  /**
   * Saves this Localization to its File.
   */
  public void save() {
    config.save();
  }

}