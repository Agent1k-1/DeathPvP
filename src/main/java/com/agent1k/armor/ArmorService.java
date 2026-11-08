package com.agent1k.armor;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ArmorService {
  private final PvpContext context;

  public ArmorService(PvpContext context) {
    this.context = context;
  }

  public boolean armor(Player p) {
    int count = 0;
    for (ItemStack item : p.getInventory().getArmorContents())
      if (item != null
          && context.plugin.getConfig().getStringList("armor.allowed").stream()
              .anyMatch(a -> item.getType().name().startsWith(a + "_"))) count++;
    return count >= context.plugin.getConfig().getInt("armor.minimum-pieces", 1);
  }
}
