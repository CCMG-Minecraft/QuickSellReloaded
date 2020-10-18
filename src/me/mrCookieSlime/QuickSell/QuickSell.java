package me.mrCookieSlime.QuickSell;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Localization;
import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibSetup.CSCoreLibLoader;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.boosters.XpBoosterListener;
import me.mrCookieSlime.QuickSell.commands.BoosterCommand;
import me.mrCookieSlime.QuickSell.commands.BoosterListCommand;
import me.mrCookieSlime.QuickSell.commands.PricesCommand;
import me.mrCookieSlime.QuickSell.commands.PrivateBoosterCommand;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import me.mrCookieSlime.QuickSell.commands.qscommand.EditorCommand;
import me.mrCookieSlime.QuickSell.commands.qscommand.MainCommand;
import me.mrCookieSlime.QuickSell.commands.qscommand.NpcLinkCommands;
import me.mrCookieSlime.QuickSell.commands.qscommand.ReloadCommand;
import me.mrCookieSlime.QuickSell.commands.qscommand.StopBoostersCommand;
import me.mrCookieSlime.QuickSell.commands.SellAllCommand;
import me.mrCookieSlime.QuickSell.commands.SellCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
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

  private PaperCommandManager paperCommandManager;

  private boolean citizens = false;

  public static void registerSellEvent(SellEvent event) {
    events.add(event);
  }

  public static List<SellEvent> getSellEvents() {
    return events;
  }

  public static QuickSell getInstance() {
    return instance;
  }

  public ShopEditor getEditor() {
    return editor;
  }

  public Config getNpcs() {
    return npcs;
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

      registerCommandManager();
    }
  }

  private void registerCommands(BaseCommand... commands) {
    for (BaseCommand command : commands) {
      paperCommandManager.registerCommand(command);
    }
  }

  private void registerCommandManager() {
    paperCommandManager = new PaperCommandManager(this);
    paperCommandManager.enableUnstableAPI("brigadier");
    registerCommands(
        /*
        /quicksell sub commands
         */
        new EditorCommand(),
        new MainCommand(),
        new NpcLinkCommands(),
        new ReloadCommand(),
        new StopBoostersCommand(),

        /* other commands */
        new BoosterCommand(),
        new BoosterListCommand(),
        new PricesCommand(),
        new PrivateBoosterCommand(),
        new QSBaseCommand(),
        new SellAllCommand(),
        new SellCommand()
    );
  }

  private void createScheduledTasks() {
    getServer().getScheduler().scheduleSyncDelayedTask(this,
        () -> {
        }, 0L);

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
    locale.setDefault("commands.booster.invalid-type",
        "&cInvalid Booster Type");
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

  public boolean isCitizensInstalled() {
    return citizens;
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
