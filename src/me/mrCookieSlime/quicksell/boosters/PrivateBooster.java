package me.mrCookieSlime.quicksell.boosters;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

public class PrivateBooster extends Booster {

  public PrivateBooster(BoosterType type, String owner, double multiplier, int minutes) {
    super(type, owner, multiplier, minutes);
  }

  public PrivateBooster(int id) throws ParseException {
    super(id);
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
