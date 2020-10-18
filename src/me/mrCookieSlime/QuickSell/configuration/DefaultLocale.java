package me.mrCookieSlime.QuickSell.configuration;

public class DefaultLocale {

  /**
   * Set all default locale properties for the messages.yml configuration.
   *
   * @param locale The locale instance
   */
  public static void setDefaultLocale(Localization locale) {
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
}
