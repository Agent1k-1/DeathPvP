package com.agent1k.core.combat;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CombatService {
  private final PvpContext context;

  public CombatService(PvpContext context) {
    this.context = context;
  }

  public void combat(Player p, String key) {
    for (String cmd : context.plugin.getConfig().getStringList("combat." + key + "-commands"))
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", p.getName()));
  }
}
