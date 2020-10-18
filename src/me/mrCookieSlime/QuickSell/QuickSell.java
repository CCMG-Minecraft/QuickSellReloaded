package me.mrCookieSlime.QuickSell;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Localization;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.QuickSell.SellEvent.Type;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.BoosterMenu;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.boosters.XpBoosterListener;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class QuickSell extends JavaPlugin {

  public static Config cfg;
  public static Economy economy = null;
  public static Localization locale;
  public static Map<UUID, Shop> shop;
  public static List<SellEvent> events;
  private static QuickSell instance;
  public Config npcs;
  private ShopEditor editor;

  private boolean citizens = false;
  private boolean backpacks = false;

  public static void registerSellEvent(SellEvent event) {
    events.add(event);
  }

  public static List<SellEvent> getSellEvents() {
    return events;
  }

  public static QuickSell getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    CSCoreLibLoader loader = new CSCoreLibLoader(this);
    if (loader.load()) {
      instance = this;

      shop = new HashMap<>();
      events = new ArrayList<>();
      editor = new ShopEditor(this);

      citizens = getServer().getPluginManager().isPluginEnabled("Citizens");
      npcs = new Config("plugins/QuickSell/citizens_npcs.yml");

      PluginUtils utils = createUtils();
      createLocale(utils);
      createConfig(utils);
      createListeners();
      reload();
      setupEconomy();
      createBoosters();
      createScheduledTasks();
    }
  }

  private void createScheduledTasks() {
    getServer().getScheduler().scheduleSyncDelayedTask(this,
        () -> backpacks = Bukkit.getPluginManager().isPluginEnabled("PrisonUtils"), 0L);

    getServer().getScheduler().runTaskTimer(this,
        Booster::update, 0L, cfg.getInt("boosters.refresh-every") * 20L);
  }

  private void createBoosters() {
    if (!new File(QuickSell.getInstance().getDataFolder(), "boosters/").exists()) {
      if (!new File(QuickSell.getInstance().getDataFolder(), "/boosters").mkdirs()) {
        getLogger().warning("Failed to create the boosters data storage directory.");
      }
    }

    File boostersFolder = new File(getDataFolder(), "boosters/");
    for (File file : boostersFolder.listFiles()) {
      if (file.getName().endsWith(".booster")) {
        try {
          if (new Config(file).getBoolean("private")) {
            new PrivateBooster(file);
          } else {
            new Booster(file);
          }
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void createListeners() {
    new SellListener(this);
    new XpBoosterListener(this);

    if (isCitizensInstalled()) {
      new CitizensListener(this);
    }
  }

  @NotNull
  private PluginUtils createUtils() {
    PluginUtils utils = new PluginUtils(this);
    utils.setupConfig();
    utils.setupMetrics();
    utils.setupLocalization();
    return utils;
  }

  private void createLocale(PluginUtils utils) {
    locale = utils.getLocalization();
    setDefaultLocaleProperties();
    locale.save();
  }

  private void createConfig(PluginUtils utils) {
    cfg = utils.getConfig();
    if (cfg.contains("options.open-only-shop-with-permission")) {
      cfg.setValue("shop.enable-hierarchy",
          cfg.getBoolean("options.open-only-shop-with-permission"));
      cfg.setValue("options.open-only-shop-with-permission", null);
      cfg.save();
    }

    if (cfg.contains("boosters.same-multiplier-increases-time")) {
      cfg.setValue("boosters.extension-mode",
          cfg.getBoolean("boosters.same-multiplier-increases-time"));
      cfg.setValue("boosters.same-multiplier-increases-time", null);
      cfg.save();
    }

    if (cfg.getBoolean("shop.enable-logging")) {
      registerSellEvent((p, type, itemsSold, money) -> {
        SellProfile profile = SellProfile.getProfile(p);
        profile.storeTransaction(type, itemsSold, money);
      });
    }
  }

  /**
   * Set the default locale properties.
   */
  private void setDefaultLocaleProperties() {
    locale.setDefault("messages.sell", "&a&l+ ${MONEY} &7[ &eSold &o{ITEMS} &eItems&7 ]");
    locale.setDefault("messages.no-access", "&4You do not have access to this Shop");
    locale.setDefault("messages.total", "&2TOTAL: &6+ ${MONEY}");
    locale.setDefault("messages.get-nothing",
        "&4Sorry, but you will get nothing for these Items :(");
    locale.setDefault("messages.dropped",
        "&cYou have been given back some of your Items because you could not sell them...");
    locale.setDefault("messages.no-permission",
        "&cYou do not have the required Permission to do this!");
    locale.setDefault("commands.booster.permission",
        "&cYou do not have permission to activate a Booster!");
    locale.setDefault("commands.permission", "&cYou do not have permission for this!");
    locale.setDefault("commands.usage", "&4Usage: &c%usage%");
    locale.setDefault("commands.reload.done", "&7All Shops have been reloaded!");
    locale.setDefault("messages.unknown-shop", "&cUnknown Shop!");
    locale.setDefault("messages.no-items", "&cSorry, but you have no Items that can be sold!");
    locale.setDefault("commands.price-set",
        "&7%item% is now worth &a$%price% &7in the Shop %shop%");
    locale.setDefault("commands.shop-created",
        "&7You successfully created a new Shop called &b%shop%");
    locale.setDefault("commands.shop-deleted",
        "&cYou successfully deleted the Shop called &4%shop%");
    locale.setDefault("menu.accept", "&a> Click to sell");
    locale.setDefault("menu.estimate", "&e> Click to estimate");
    locale.setDefault("menu.cancel", "&c> Click to cancel");
    locale.setDefault("menu.title", "&6&l$ Sell your Items $");
    locale.setDefault("messages.estimate", "&eYou will get &6${MONEY} &efor these Items");
    locale.setDefault("commands.sellall.usage", "&4Usage: &c/sellall <Shop>");
    locale.setDefault("commands.disabled", "&cThis command has been disabled");
    locale.setDefault("booster.reset", "&cReset %player%'s multiplier to 1.0x");
    locale.setDefault("boosters.reset", "&cReset all Boosters to 1.0x");
    locale.setDefault("commands.prices.usage", "&4Usage: &c/prices <Shop>");
    locale.setDefault("commands.prices.permission", "&cYou do not have permission for this!");
    locale.setDefault("editor.create-shop", "&a&l! &7Please type in a Name for your Shop in Chat!",
        "&7&oColor Codes are supported!");
    locale.setDefault("editor.rename-shop", "&a&l! &7Please type in a Name for your Shop in Chat!",
        "&7&oColor Codes are supported!");
    locale.setDefault("editor.renamed-shop", "&a&l! &7Successfully renamed Shop!");
    locale.setDefault("editor.set-permission-shop",
        "&a&l! &7Please type in a Permission for your Shop!",
        "&7&oType \"none\" to specify no Permission");
    locale.setDefault("editor.permission-set-shop",
        "&a&l! &7Successfully specified a Permission for your Shop!");

    locale.setDefault("messages.booster-use.MONETARY",
        "&a&l+ ${MONEY} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");
    locale.setDefault("messages.booster-use.EXP");
    locale.setDefault("messages.booster-use.MCMMO");
    locale.setDefault("messages.booster-use.PRISONGEMS",
        "&7+ &a{GEMS} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");

    locale.setDefault("messages.pbooster-use.MONETARY",
        "&a&l+ ${MONEY} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");
    locale.setDefault("messages.pbooster-use.EXP");
    locale.setDefault("messages.pbooster-use.MCMMO");
    locale.setDefault("messages.pbooster-use.PRISONGEMS",
        "&7+ &a{GEMS} &7(&e%multiplier%x Booster &7&oHover for more Info &7)");

    locale.setDefault("booster.extended.MONETARY",
        "&6%player% &ehas extended the %multiplier%x Booster (Money) for %time% more Minute/s");
    locale.setDefault("booster.extended.EXP",
        "&6%player% &ehas extended the %multiplier%x Booster (Experience) for %time% more "
            + "Minute/s");
    locale.setDefault("booster.extended.MCMMO",
        "&6%player% &ehas extended the %multiplier%x Booster (mcMMO) for %time% more Minute/s");
    locale.setDefault("booster.extended.PRISONGEMS",
        "&6%player% &ehas extended the %multiplier%x Booster (Gems) for %time% more Minute/s");

    locale.setDefault("pbooster.extended.MONETARY",
        "&eYour %multiplier%x Booster (Money) has been extended for %time% more Minute/s");
    locale.setDefault("pbooster.extended.EXP",
        "&eYour %multiplier%x Booster (Experience) has been extended for %time% more Minute/s");
    locale.setDefault("pbooster.extended.MCMMO",
        "&eYour %multiplier%x Booster (mcMMO) has been extended for %time% more Minute/s");
    locale.setDefault("pbooster.extended.PRISONGEMS",
        "&eYour %multiplier%x Booster (Gems) has been extended for %time% more Minute/s");

    locale.setDefault("booster.activate.MONETARY",
        "&6&l%player% &ehas activated a %multiplier%x Booster (Money) for %time% Minute/s");
    locale.setDefault("booster.activate.EXP",
        "&6&l%player% &ehas activated a %multiplier%x Booster (Experience) for %time% Minute/s");
    locale.setDefault("booster.activate.MCMMO",
        "&6&l%player% &ehas activated a %multiplier%x Booster (mcMMO) for %time% Minute/s");
    locale.setDefault("booster.activate.PRISONGEMS",
        "&6&l%player% &ehas activated a %multiplier%x Booster (Gems) for %time% Minute/s");

    locale.setDefault("booster.deactivate.MONETARY",
        "&4%player%'s &c%multiplier%x Booster (Money) wore off!");
    locale.setDefault("booster.deactivate.EXP",
        "&4%player%'s &c%multiplier%x Booster (Experience) wore off!");
    locale.setDefault("booster.deactivate.MCMMO",
        "&4%player%'s &c%multiplier%x Booster (mcMMO) wore off!");
    locale.setDefault("booster.deactivate.PRISONGEMS",
        "&4%player%'s &c%multiplier%x Booster (Gems) wore off!");

    locale.setDefault("pbooster.activate.MONETARY",
        "&eYou have been given a %multiplier%x Booster (Money) for %time% Minute/s");
    locale.setDefault("pbooster.activate.EXP",
        "&eYou have been given a %multiplier%x Booster (Experience) for %time% Minute/s");
    locale.setDefault("pbooster.activate.MCMMO",
        "&eYou have been given a %multiplier%x Booster (mcMMO) for %time% Minute/s");
    locale.setDefault("pbooster.activate.PRISONGEMS",
        "&eYou have been given a %multiplier%x Booster (Gems) for %time% Minute/s");

    locale.setDefault("pbooster.deactivate.MONETARY",
        "&4Your &c%multiplier%x Booster (Money) wore off!");
    locale.setDefault("pbooster.deactivate.EXP",
        "&4Your &c%multiplier%x Booster (Experience) wore off!");
    locale.setDefault("pbooster.deactivate.MCMMO",
        "&4Your &c%multiplier%x Booster (mcMMO) wore off!");
    locale.setDefault("pbooster.deactivate.PRISONGEMS",
        "&4Your &c%multiplier%x Booster (Gems) wore off!");
  }

  @Override
  public void onDisable() {
    cfg = null;
    shop = null;
    economy = null;
    locale = null;
    events = null;

    for (SellProfile profile : SellProfile.profiles.values()) {
      profile.save();
    }

    SellProfile.profiles = null;
    Booster.active = null;
  }

  /**
   * Setup the economy provider.
   */
  private void setupEconomy() {
    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
        .getRegistration(Economy.class);
    if (economyProvider != null) {
      economy = economyProvider.getProvider();
    }

  }

  /**
   * Handle commands (TODO: Put this in ACF).
   *
   * @param sender The command sender
   * @param cmd    The command itself
   * @param label  The command label
   * @param args   Any arguments sent with the command
   * @return Whether or not the command executed successfully.
   */
  @Override
  public boolean onCommand(
      @NotNull CommandSender sender,
      Command cmd,
      @NotNull String label,
      String[] args
  ) {
    if (cmd.getName().equalsIgnoreCase("sell")) {
      if (cfg.getBoolean("options.enable-commands")) {
        if (sender instanceof Player) {
          if (Shop.list().size() == 1) {
            ShopMenu.open((Player) sender, Shop.list().get(0));
          } else if (args.length > 0) {
            Shop shop = Shop.getShop(args[0]);
            if (shop != null) {
              if (shop.hasUnlocked((Player) sender)) {
                ShopMenu.open((Player) sender, shop);
              } else {
                QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
              }
            } else {
              ShopMenu.openMenu((Player) sender);
            }
          } else if (cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) != null) {
              ShopMenu.open((Player) sender,
                  Objects.requireNonNull(Shop.getHighestShop((Player) sender)));
            } else {
              QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
            }
          } else {
            ShopMenu.openMenu((Player) sender);
          }
        } else {
          sender.sendMessage(
              ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
        }
      } else {
        locale.sendTranslation(sender, "commands.disabled", false);
      }
    } else if (cmd.getName().equalsIgnoreCase("sellall")) {
      if (cfg.getBoolean("options.enable-commands")) {
        if (sender instanceof Player) {
          if (args.length > 0) {
            Shop shop = Shop.getShop(args[0]);
            if (shop != null) {
              if (shop.hasUnlocked((Player) sender)) {
                shop.sellAll((Player) sender, Type.SELLALL);
              } else {
                QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
              }
            } else {
              locale.sendTranslation(sender, "messages.unknown-shop", false);
            }
          } else if (cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) != null) {
              Objects.requireNonNull(Shop.getHighestShop((Player) sender))
                  .sellAll((Player) sender, Type.SELLALL);
            } else {
              QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
            }
          } else {
            locale.sendTranslation(sender, "commands.sellall.usage", false);
          }
        } else {
          sender.sendMessage(
              ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
        }
      } else {
        locale.sendTranslation(sender, "commands.disabled", false);
      }
    } else if (cmd.getName().equalsIgnoreCase("prices")) {
      if (sender instanceof Player) {
        if (sender.hasPermission("QuickSell.prices")) {
          if (args.length > 0) {
            Shop shop = Shop.getShop(args[0]);
            if (shop != null) {
              if (shop.hasUnlocked((Player) sender)) {
                shop.showPrices((Player) sender);
              } else {
                QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
              }
            } else {
              locale.sendTranslation(sender, "messages.unknown-shop", false);
            }
          } else if (cfg.getBoolean("options.open-only-shop-with-permission")) {
            if (Shop.getHighestShop((Player) sender) != null) {
              Objects.requireNonNull(Shop.getHighestShop((Player) sender))
                  .showPrices((Player) sender);
            } else {
              QuickSell.locale.sendTranslation(sender, "messages.no-access", false);
            }
          } else {
            locale.sendTranslation(sender, "commands.prices.usage", false);
          }
        } else {
          locale.sendTranslation(sender, "commands.prices.permission", false);
        }
      } else {
        sender.sendMessage(
            ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
      }
    } else if (cmd.getName().equalsIgnoreCase("booster")) {
      if (args.length == 4) {
        BoosterType type =
            args[0].equalsIgnoreCase("all") ? null : BoosterType.valueOf(args[0].toUpperCase());
        if ((type != null || args[0].equalsIgnoreCase("all")) && (
            sender instanceof ConsoleCommandSender || sender.hasPermission("QuickSell.booster"))) {
          try {
            if (type != null) {
              Booster booster = new Booster(type, args[1], Double.parseDouble(args[2]),
                  Integer.parseInt(args[3]));
              booster.activate();
            } else {
              for (BoosterType bt : BoosterType.values()) {
                Booster booster = new Booster(bt, args[1], Double.parseDouble(args[2]),
                    Integer.parseInt(args[3]));
                booster.activate();
              }
            }
          } catch (NumberFormatException x) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&4Usage: &c/booster <all/monetary/prisongems/exp/mcmmo/casino> <Name of the "
                    + "Player> <Multiplier> <Duration in Minutes>"));
          }
        } else {
          locale.sendTranslation(sender, "commands.booster.permission", false);
        }
      } else {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&4Usage: &c/booster <all/monetary/prisongems/exp/mcmmo/casino> <Name of the Player> "
                + "<Multiplier> <Duration in Minutes>"));
      }
    } else if (cmd.getName().equalsIgnoreCase("pbooster")) {
      if (args.length == 4) {
        BoosterType type =
            args[0].equalsIgnoreCase("all") ? null : BoosterType.valueOf(args[0].toUpperCase());
        if ((type != null || args[0].equalsIgnoreCase("all")) && (
            sender instanceof ConsoleCommandSender || sender.hasPermission("QuickSell.booster"))) {
          try {
            if (type != null) {
              PrivateBooster booster = new PrivateBooster(
                  type, args[1], Double.parseDouble(args[2]), Integer.parseInt(args[3])
              );
              booster.activate();
            } else {
              for (BoosterType bt : BoosterType.values()) {
                PrivateBooster booster = new PrivateBooster(
                    bt, args[1], Double.parseDouble(args[2]), Integer.parseInt(args[3])
                );
                booster.activate();
              }
            }
          } catch (NumberFormatException x) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&4Usage: &c/pbooster <all/monetary/prisongems/exp/mcmmo/casino> <Name of the "
                    + "Player> <Multiplier> <Duration in Minutes>"));
          }
        } else {
          locale.sendTranslation(sender, "commands.booster.permission", false);
        }
      } else {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&4Usage: &c/pbooster <all/monetary/prisongems/exp/mcmmo/casino> <Name of the Player>"
                + " <Multiplier> <Duration in Minutes>"));
      }
    } else if (cmd.getName().equalsIgnoreCase("boosters")) {
      if (sender instanceof Player) {
        BoosterMenu.showBoosterOverview((Player) sender);
      } else {
        sender.sendMessage(
            ChatColor.translateAlternateColorCodes('&', "This Command is only for Players"));
      }
    } else if (cmd.getName().equalsIgnoreCase("quicksell")) {
      if (sender instanceof ConsoleCommandSender || sender.hasPermission("QuickSell.manage")) {
        if (args.length >= 1) {
          if (args[0].equalsIgnoreCase("reload")) {
            reload();
            locale.sendTranslation(sender, "commands.reload.done", false);
          } else if (args[0].equalsIgnoreCase("editor")) {
            if (sender instanceof Player) {
              editor.openEditor((Player) sender);
            }
          } else if (args[0].equalsIgnoreCase("edit")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&4DEPRECATED! Use &c/quicksell editor &4instead!"));
            if (args.length == 4) {
              if (Shop.getShop(args[1]) != null) {
                boolean number = true;
                try {
                  Double.valueOf(args[3]);
                } catch (NumberFormatException x) {
                  number = false;
                }
                if (number) {
                  cfg.setValue("shops." + args[1] + ".price." + args[2].toUpperCase(),
                      Double.valueOf(args[3]));
                  cfg.save();
                  reload();
                  locale.sendTranslation(sender, "commands.price-set", false,
                      new Variable("%item%", args[2]), new Variable("%shop%", args[1]),
                      new Variable("%price%", args[3]));
                } else {
                  locale.sendTranslation(sender, "commands.usage", false,
                      new Variable("%usage%", "/quicksell edit <ShopName> <Item> <Price>"));
                }
              } else {
                locale.sendTranslation(sender, "messages.unknown-shop", false);
              }
            } else {
              locale.sendTranslation(sender, "commands.usage", false,
                  new Variable("%usage%", "/quicksell edit <ShopName> <Item> <Price>"));
            }
          } else if (args[0].equalsIgnoreCase("create")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&4DEPRECATED! Use &c/quicksell editor &4instead!"));
            if (args.length == 2) {
              List<String> list = new ArrayList<>();
              for (Shop shop : Shop.list()) {
                list.add(shop.getId());
              }
              list.add(args[1]);
              cfg.setValue("list", list);
              cfg.save();
              reload();
              locale.sendTranslation(sender, "commands.shop-created", false,
                  new Variable("%shop%", args[1]));
            } else {
              locale.sendTranslation(sender, "commands.usage", false,
                  new Variable("%usage%", "/quicksell create <ShopName>"));
            }
          } else if (args[0].equalsIgnoreCase("delete")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&4DEPRECATED! Use &c/quicksell editor &4instead!"));
            if (Shop.getShop(args[1]) == null) {
              locale.sendTranslation(sender, "messages.unknown-shop", false);
            } else if (args.length == 2) {
              List<String> list = new ArrayList<>();
              for (Shop shop : Shop.list()) {
                list.add(shop.getId());
              }
              list.remove(args[1]);
              cfg.setValue("list", list);
              cfg.save();
              reload();
              locale.sendTranslation(sender, "commands.shop-deleted", false,
                  new Variable("%shop%", args[1]));
            } else {
              locale.sendTranslation(sender, "commands.usage", false,
                  new Variable("%usage%", "/quicksell delete <ShopName>"));
            }
          } else if (args[0].equalsIgnoreCase("linknpc")) {
            if (!isCitizensInstalled()) {
              sender.sendMessage(ChatColor
                  .translateAlternateColorCodes('&', "&4You do not have Citizens installed!"));
              return true;
            }

            if (args.length == 3) {
              NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
              if (npc == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cYou must select an NPC before linking it!"));
              } else if (Shop.getShop(args[1]) == null) {
                locale.sendTranslation(sender, "messages.unknown-shop", false);
              } else if (!args[2].equalsIgnoreCase("sell") && !args[2]
                  .equalsIgnoreCase("sellall")) {
                npcs.setValue(String.valueOf(npc.getId()), args[1] + " ; " + args[2].toUpperCase());
                npcs.save();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    npc.getName() + " &7is now a Remote &r" + StringUtils.format(args[2])
                        + "&7Shop for the Shop &r" + args[1] + "&7"));
              }
            } else {
              locale.sendTranslation(sender, "commands.usage", false,
                  new Variable("%usage%", "/quicksell linknpc <ShopName> <sell/sellall>"));
            }
          } else if (args[0].equalsIgnoreCase("unlinknpc")) {
            if (!isCitizensInstalled()) {
              sender.sendMessage(ChatColor
                  .translateAlternateColorCodes('&', "&4You do not have Citizens installed!"));
              return true;
            }

            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
            if (npc == null) {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  "&cYou must select an NPC before linking it!"));
            } else if (npcs.contains(String.valueOf(npc.getId()))) {
              npcs.setValue(String.valueOf(npc.getId()), null);
              npcs.save();
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  npc.getName() + " &cis no longer linked to any Shop!"));
            } else {
              sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                  npc.getName() + " &cis not linked to any Shop!"));
            }
          } else if (args[0].equalsIgnoreCase("stopboosters")) {
            if (args.length == 2) {
              Iterator<Booster> boosters = Booster.iterate();
              while (boosters.hasNext()) {
                Booster booster = boosters.next();
                if (booster.getAppliedPlayers().contains(args[1])) {
                  boosters.remove();
                  booster.deactivate();
                }
              }
              locale.sendTranslation(sender, "booster.reset", false,
                  new Variable("%player%", args[1]));
            } else if (args.length == 1) {
              Iterator<Booster> boosters = Booster.iterate();
              while (boosters.hasNext()) {
                Booster booster = boosters.next();
                boosters.remove();
                booster.deactivate();
              }
              locale.sendTranslation(sender, "boosters.reset", false);
            } else {
              locale.sendTranslation(sender, "commands.usage", false,
                  new Variable("%usage%", "/quicksell stopboosters <Player>"));
            }
          } else {
            sendHelpMessager(sender);
          }
        } else {
          sendHelpMessager(sender);
        }
      } else {
        locale.sendTranslation(sender, "commands.permission", false);
      }
    }
    return true;
  }

  private void sendHelpMessager(CommandSender sender) {
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&e&lQuickSell v" + getDescription().getVersion() + " by &6mrCookieSlime"));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    sender.sendMessage(ChatColor
        .translateAlternateColorCodes('&', "&7⇨ /quicksell: &bDisplays this Help Menu"));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&7⇨ /quicksell reload: &bReloads all of QuickSell's Files and Systems"));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&7⇨ /quicksell editor: &bOpens up the Ingame Shop Editor"));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&7⇨ /quicksell stopboosters [Player]: &bStops certain Boosters"));
    if (isCitizensInstalled()) {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
          "&7⇨ /quicksell linknpc <Shop> <sell/sellall>: &bLinks a Citizens NPC to a Shop"));
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
          "&7⇨ /quicksell unlinknpc: &bUnlinks your selected NPC from a Shop"));
    }
  }

  public boolean isCitizensInstalled() {
    return citizens;
  }

  public boolean isPrisonUtilsInstalled() {
    return backpacks;
  }

  /**
   * Reload the plugin.
   */
  public void reload() {
    cfg.reload();
    Shop.reset();

    for (String shop : cfg.getStringList("list")) {
      if (!shop.equalsIgnoreCase("")) {
        cfg.setDefaultValue("shops." + shop + ".name", "&9" + shop);
        cfg.setDefaultValue("shops." + shop + ".amount", 1);
        cfg.setDefaultValue("shops." + shop + ".itemtype", "CHEST");
        cfg.setDefaultValue("shops." + shop + ".lore", new ArrayList<String>());
        cfg.setDefaultValue("shops." + shop + ".permission", "QuickSell.shop." + shop);
        cfg.setDefaultValue("shops." + shop + ".inheritance", new ArrayList<String>());

        if (cfg.getBoolean("options.pregenerate-all-item-prices")) {
          for (Material m : Material.values()) {
            if (m != Material.AIR) {
              cfg.setDefaultValue("shops." + shop + ".price." + m.toString(), 0.0);
            }
          }
        } else {
          cfg.setDefaultValue("shops." + shop + ".price.COBBLESTONE", 0.0);
        }
        new Shop(shop);
      } else {
        new Shop();
      }
    }
    cfg.save();
  }

}
