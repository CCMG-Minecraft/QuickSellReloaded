package me.mrCookieSlime.QuickSell.boosters;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Variable;
import me.mrCookieSlime.QuickSell.QuickSell;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class XpBoosterListener implements Listener {

  public XpBoosterListener(QuickSell plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  /**
   * Give extra experience whenever you gain experience if relevant boosters are active.
   *
   * @param e The bukkit event
   */
  @EventHandler
  public void onXpGain(PlayerExpChangeEvent e) {
    Player p = e.getPlayer();
    int xp = e.getAmount();
    for (Booster booster : Booster.getBoosters(p.getName())) {
      if (booster.getType().equals(BoosterType.EXP)) {
        if (!booster.isSilent()) {
          booster.sendMessage(p,
              new Variable("{XP}",
                  String.valueOf((float) (xp * (booster.getBoosterMultiplier() - 1.0)))));
        }
        xp = (int) (xp + xp * (booster.getBoosterMultiplier() - 1));
      }
    }

    e.setAmount(xp);
  }

}
