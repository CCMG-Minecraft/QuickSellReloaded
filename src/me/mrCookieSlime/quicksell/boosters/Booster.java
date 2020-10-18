package me.mrCookieSlime.quicksell.boosters;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.ClickAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Chat.TellRawMessage.HoverAction;
import me.mrCookieSlime.CSCoreLibPlugin.general.Clock;
import me.mrCookieSlime.CSCoreLibPlugin.general.Math.DoubleHandler;
import me.mrCookieSlime.quicksell.QuickSell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Booster {

  public static List<Booster> active = new ArrayList<>();
  public String owner;
  BoosterType type;
  int id;
  int minutes;
  double multiplier;
  Date timeout;
  Config cfg;
  boolean silent;
  boolean infinite;
  Map<String, Integer> contributors = new HashMap<>();

  /**
   * A booster is a system which allows a player, or the entire server to receive a temporary or
   * permanent bonus in items whenever they hit certain objectives, such as gaining experience,
   * McMMO levels or in QuickSell item shops.
   *
   * @param type       The booster type
   * @param owner      The owner of the booster
   * @param multiplier The multiplier of the booster
   * @param minutes    How long the booster should last (in minutes)
   */
  public Booster(BoosterType type, String owner, double multiplier, int minutes) {
    this.type = type;
    this.minutes = minutes;
    this.multiplier = multiplier;
    this.owner = owner;
    this.timeout = new Date(System.currentTimeMillis() + minutes * 60 * 1000);
    this.silent = false;
    this.infinite = false;

    contributors.put(owner, minutes);
  }

  /**
   * Retrieve a stored booster in the persistent data.
   *
   * @param id The ID of the booster.
   * @throws ParseException If there is malformed configuration values (specifically in dates)
   */
  public Booster(int id) throws ParseException {
    active.add(this);
    this.id = id;
    this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
    if (cfg.contains("type")) {
      this.type = BoosterType.valueOf(cfg.getString("type"));
    } else {
      cfg.setValue("type", BoosterType.MONETARY.toString());
      cfg.save();
      this.type = BoosterType.MONETARY;
    }

    this.minutes = cfg.getInt("minutes");
    this.multiplier = (Double) cfg.getValue("multiplier");
    this.owner = cfg.getString("owner");
    this.timeout = new SimpleDateFormat("yyyy-MM-dd-HH-mm").parse(cfg.getString("timeout"));
    this.silent = false;
    this.infinite = false;

    if (cfg.contains("contributors." + owner)) {
      for (String key : cfg.getKeys("contributors")) {
        contributors.put(key, cfg.getInt("contributors." + key));
      }
    } else {
      contributors.put(owner, minutes);
      writeContributors();
    }
  }

  public static Iterator<Booster> iterate() {
    return active.iterator();
  }

  /**
   * Update all boosters.
   */
  public static void update() {
    Iterator<Booster> boosters = Booster.iterate();
    while (boosters.hasNext()) {
      Booster booster = boosters.next();
      if (new Date().after(booster.getDeadLine())) {
        boosters.remove();
        booster.deactivate();
      }
    }
  }

  /**
   * Get multiplier for a player.
   *
   * @param player The player
   * @return The total multiplier
   * @deprecated Use {@link Booster#getMultiplier(String, BoosterType)} instead
   */
  @Deprecated
  public static Double getMultiplier(String player) {
    return getMultiplier(player, BoosterType.MONETARY);
  }

  /**
   * Get the multiplier for a player of a booster type.
   *
   * @param player The player to get multipliers for
   * @param type   The type of booster to get multipliers for
   * @return The total multipler for the player
   */
  public static double getMultiplier(String player, BoosterType type) {
    double multiplier = 1.0;
    for (Booster booster : getBoosters(player, type)) {
      multiplier = multiplier * booster.getBoosterMultiplier();
    }
    return DoubleHandler.fixDouble(multiplier, 2);
  }

  /**
   * Get all applicable boosters for a player.
   *
   * @param player The player
   * @return A list of all boosters which the player can benefit from
   */
  public static List<Booster> getBoosters(String player) {
    update();
    List<Booster> boosters = new ArrayList<>();

    for (Booster booster : active) {
      if (booster.getAppliedPlayers().contains(player)) {
        boosters.add(booster);
      }
    }
    return boosters;
  }

  /**
   * Get all applicable boosters for a player (for a certain type).
   *
   * @param player The player
   * @param type   The type of booster which is applicable to this query
   * @return A list of all boosters applicable to the player
   */
  public static List<Booster> getBoosters(String player, BoosterType type) {
    update();
    List<Booster> boosters = new ArrayList<>();

    for (Booster booster : active) {
      if (booster.getAppliedPlayers().contains(player) && booster.getType().equals(type)) {
        boosters.add(booster);
      }
    }
    return boosters;
  }

  /**
   * Get how much time is left on all boosters applicable to players.
   *
   * @param player The player to check for
   * @return The time left (in minutes)
   * @deprecated magic value.
   */
  @Deprecated
  public static long getTimeLeft(String player) {
    long timeleft = 0;
    for (Booster booster : getBoosters(player)) {
      timeleft = timeleft + booster.formatTime();
    }
    return timeleft;
  }

  /**
   * Get this booster's multiplier.
   *
   * @return The multiplier for this booster
   */
  public Double getBoosterMultiplier() {
    return this.multiplier;
  }

  private void writeContributors() {
    for (Map.Entry<String, Integer> entry : contributors.entrySet()) {
      cfg.setValue("contributors." + entry.getKey(), entry.getValue());
    }

    cfg.save();
  }

  /**
   * Activate the booster.
   */
  public void activate() {
    if (QuickSell.cfg.getBoolean("boosters.extension-mode")) {
      for (Booster booster : active) {
        if (booster.getType().equals(this.type)
            && Double.compare(booster.getBoosterMultiplier(), getBoosterMultiplier()) == 0) {
          if ((this instanceof PrivateBooster && booster instanceof PrivateBooster) || (
              !(this instanceof PrivateBooster) && !(booster instanceof PrivateBooster))) {
            booster.extend(this);
            if (!silent) {
              if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) {
                QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()),
                    "pbooster.extended." + type.toString(), false,
                    new Variable("%time%", String.valueOf(this.getDuration())),
                    new Variable("%multiplier%", String.valueOf(this.getBoosterMultiplier())));
              } else {
                for (String message : QuickSell.local
                    .getTranslation("booster.extended." + type.toString())) {
                  Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                      message.replace("%player%", this.getOwner())
                          .replace("%time%", String.valueOf(this.getDuration()))
                          .replace("%multiplier%", String.valueOf(this.getBoosterMultiplier()))));
                }
              }
            }
            return;
          }
        }
      }
    }

    if (!infinite) {
      for (int i = 0; i < 1000; i++) {
        if (!new File("data-storage/QuickSell/boosters/" + i + ".booster").exists()) {
          this.id = i;
          break;
        }
      }
      this.cfg = new Config(new File("data-storage/QuickSell/boosters/" + id + ".booster"));
      cfg.setValue("type", type.toString());
      cfg.setValue("owner", getOwner());
      cfg.setValue("multiplier", multiplier);
      cfg.setValue("minutes", minutes);
      cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
      cfg.setValue("private", this instanceof PrivateBooster);

      writeContributors();
    }

    active.add(this);
    if (!silent) {
      if (this instanceof PrivateBooster && Bukkit.getPlayer(getOwner()) != null) {
        QuickSell.local
            .sendTranslation(Bukkit.getPlayer(getOwner()), "pbooster.activate." + type.toString(),
                false, new Variable("%time%", String.valueOf(this.getDuration())),
                new Variable("%multiplier%", String.valueOf(this.getBoosterMultiplier())));
      } else {
        for (String message : QuickSell.local
            .getTranslation("booster.activate." + type.toString())) {
          Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
              message.replace("%player%", this.getOwner())
                  .replace("%time%", String.valueOf(this.getDuration()))
                  .replace("%multiplier%", String.valueOf(this.getBoosterMultiplier()))));
        }
      }
    }
  }

  /**
   * Extend the booster (by another booster's duration).
   *
   * @param booster The booster to get the duration of to add.
   */
  public void extend(Booster booster) {
    addTime(booster.getDuration());

    int minutes =
        contributors.getOrDefault(booster.getOwner(), 0);
    minutes = minutes + booster.getDuration();
    contributors.put(booster.getOwner(), minutes);

    writeContributors();
  }

  /**
   * Deactivate the booster.
   */
  public void deactivate() {
    if (!silent) {
      if (this instanceof PrivateBooster) {
        if (Bukkit.getPlayer(getOwner()) != null) {
          QuickSell.local.sendTranslation(Bukkit.getPlayer(getOwner()),
              "pbooster.deactivate." + type.toString(), false,
              new Variable("%time%", String.valueOf(this.getDuration())),
              new Variable("%multiplier%", String.valueOf(this.getBoosterMultiplier())));
        }
      } else {
        for (String message : QuickSell.local
            .getTranslation("booster.deactivate." + type.toString())) {
          Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
              message.replace("%player%", this.getOwner())
                  .replace("%time%", String.valueOf(this.getDuration()))
                  .replace("%multiplier%", String.valueOf(this.getBoosterMultiplier()))));
        }
      }
    }
    if (!infinite) {
      //noinspection ResultOfMethodCallIgnored
      new File("data-storage/QuickSell/boosters/" + getId() + ".booster").delete();
    }
    active.remove(this);
  }

  public String getOwner() {
    return this.owner;
  }

  public int getDuration() {
    return this.minutes;
  }

  public Date getDeadLine() {
    return this.timeout;
  }

  public int getId() {
    return this.id;
  }

  public long formatTime() {
    return ((getDeadLine().getTime() - Clock.getCurrentDate().getTime()) / (1000 * 60));
  }

  /**
   * Add minutes to a booster.
   *
   * @param minutes How many minutes to add to the booster
   */
  public void addTime(int minutes) {
    timeout = new Date(timeout.getTime() + minutes * 60 * 1000);
    cfg.setValue("timeout", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(timeout));
    cfg.save();
  }

  /**
   * Get all players who the booster applies to.
   *
   * @return A list of player names whom the booster applies to.
   */
  public List<String> getAppliedPlayers() {
    List<String> players = new ArrayList<>();
    for (Player p : Bukkit.getOnlinePlayers()) {
      players.add(p.getName());
    }
    return players;
  }

  public String getMessage() {
    return "messages.booster-use." + type.toString();
  }

  public BoosterType getType() {
    return this.type;
  }

  public boolean isSilent() {
    return silent;
  }

  /**
   * Get the booster name.
   *
   * @return "Booster (TYPE)" as a string
   */
  public String getUniqueName() {
    switch (type) {
      case EXP:
        return "Booster (Experience)";
      case MONETARY:
        return "Booster (Money)";
      default:
        return "Booster";
    }
  }

  /**
   * Check whether or not the booster is private or public.
   *
   * @return True if private, False if public.
   */
  public boolean isPrivate() {
    return this instanceof PrivateBooster;
  }

  public boolean isInfinite() {
    return this.infinite;
  }

  public Map<String, Integer> getContributors() {
    return this.contributors;
  }

  /**
   * Send a message to a player.
   *
   * @param player    The player to send the message to
   * @param variables Any variables in the message
   */
  public void sendMessage(Player player, Variable... variables) {
    List<String> messages = QuickSell.local.getTranslation(getMessage());
    if (messages.isEmpty()) {
      return;
    }
    try {
      String message = ChatColor.translateAlternateColorCodes('&',
          messages.get(0).replace("%multiplier%", String.valueOf(this.multiplier))
              .replace("%minutes%", String.valueOf(this.formatTime())));
      for (Variable v : variables) {
        message = v.apply(message);
      }
      new TellRawMessage()
          .addText(message)
          .addClickEvent(ClickAction.RUN_COMMAND, "/boosters")
          .addHoverEvent(HoverAction.SHOW_TEXT, BoosterMenu.getTellRawMessage(this))
          .send(player);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
