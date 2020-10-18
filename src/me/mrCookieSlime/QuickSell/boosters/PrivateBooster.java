package me.mrCookieSlime.QuickSell.boosters;

import java.io.File;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class PrivateBooster extends Booster {

  public PrivateBooster(BoosterType type, String owner, double multiplier, int minutes) {
    super(type, owner, multiplier, minutes);
  }

  public PrivateBooster(File config) throws ParseException {
    super(config);
  }

  @Override
  public List<String> getAppliedPlayers() {
    return Collections.singletonList(owner);
  }

  @Override
  public String getMessage() {
    return "messages.pbooster-use." + type.toString();
  }

}
