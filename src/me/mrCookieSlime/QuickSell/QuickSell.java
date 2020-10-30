package me.mrCookieSlime.QuickSell;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.mrCookieSlime.QuickSell.boosters.Booster;
import me.mrCookieSlime.QuickSell.boosters.PrivateBooster;
import me.mrCookieSlime.QuickSell.boosters.XpBoosterListener;
import me.mrCookieSlime.QuickSell.commands.BoosterCommand;
import me.mrCookieSlime.QuickSell.commands.BoosterListCommand;
import me.mrCookieSlime.QuickSell.commands.PricesCommand;
import me.mrCookieSlime.QuickSell.commands.PrivateBoosterCommand;
import me.mrCookieSlime.QuickSell.commands.QSBaseCommand;
import me.mrCookieSlime.QuickSell.commands.QSCommand.MainCommand;
import me.mrCookieSlime.QuickSell.commands.QSCommand.NpcLinkCommands;
import me.mrCookieSlime.QuickSell.commands.QSCommand.ReloadCommand;
import me.mrCookieSlime.QuickSell.commands.QSCommand.StopBoostersCommand;
import me.mrCookieSlime.QuickSell.commands.SellAllCommand;
import me.mrCookieSlime.QuickSell.commands.SellCommand;
import me.mrCookieSlime.QuickSell.configuration.Config;
import me.mrCookieSlime.QuickSell.configuration.DefaultLocale;
import me.mrCookieSlime.QuickSell.configuration.Localization;
import me.mrCookieSlime.QuickSell.listener.CitizensListener;
import me.mrCookieSlime.QuickSell.listener.SellListener;
import me.mrCookieSlime.QuickSell.shop.Shop;
import me.mrCookieSlime.QuickSell.transactions.SellEvent;
import me.mrCookieSlime.QuickSell.transactions.SellProfile;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class QuickSell extends JavaPlugin {

  public static Config cfg;
  public static Economy economy = null;
  public static Localization locale;
  public static Map<UUID, Shop> shop;
  public static List<SellEvent> events;
  private static QuickSell instance;
  public Config npcs;

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

  public static Localization getLocale() {
    return locale;
  }

  public static Config getQuickSellConfig() {
    return cfg;
  }

  public Config getNpcs() {
    return npcs;
  }

  @Override
  public void onEnable() {
    instance = this;

    // BStats
    new Metrics(this, 9148);

    shop = new HashMap<>();
    events = new ArrayList<>();

    citizens = getServer().getPluginManager().isPluginEnabled("Citizens");
    npcs = new Config("plugins/QuickSell/citizens_npcs.yml");

    createLocale();
    createConfig();
    createListeners();
    reload();
    setupEconomy();
    createBoosters();
    createScheduledTasks();

    registerCommandManager();
  }

  private void registerCommands(BaseCommand... commands) {
    for (BaseCommand command : commands) {
      paperCommandManager.registerCommand(command);
    }
  }

  private void registerCommandManager() {
    paperCommandManager = new PaperCommandManager(this);
    registerCommands(
        /*
        /quicksell sub commands
         */
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

    setupCommandCompletions();
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

  private void createLocale() {
    locale = new Localization(this);
    DefaultLocale.setDefaultLocale(locale);
    locale.save();
  }

  private void createConfig() {
    cfg = new Config(this);
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

  @SuppressWarnings("CheckStyle")
  private void setupCommandCompletions() {
    paperCommandManager.getCommandCompletions().registerAsyncCompletion("allshops", c -> {
      List<String> shopIds = new ArrayList<>();
      for (Shop shop : Shop.list()) {
        try {
          shopIds.add(shop.getId());
        } catch (NullPointerException ignored) {
        }
      }
      return Collections.unmodifiableList(shopIds);
    });

    paperCommandManager.getCommandCompletions().registerAsyncCompletion("availableshops", c -> {
      List<String> shopIds = new ArrayList<>();
      for (Shop shop : Shop.list()) {
        try {
          if (shop.hasUnlocked(c.getPlayer())) {
            shopIds.add(shop.getId());
          }
        } catch (NullPointerException ignored) {
        }
      }
      return Collections.unmodifiableList(shopIds);
    });
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
    locale.reload();
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
