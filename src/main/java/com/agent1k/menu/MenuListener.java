package com.agent1k.menu;

import com.agent1k.action.Action;
import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class MenuListener implements Listener {
  private final PvpContext context;

  public MenuListener(PvpContext context) {
    this.context = context;
  }

  @EventHandler
  public void click(InventoryClickEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof Menu m) {
      e.setCancelled(true);
      if (e.getWhoClicked() instanceof Player p
          && e.getRawSlot() >= 0
          && e.getRawSlot() < m.inventory.getSize())
        for (String raw : m.bindings.getOrDefault(e.getRawSlot(), List.of())) {
          String[] pair = raw.split(":", 2);
          Action action = context.actions.get(pair[0].trim().toUpperCase(Locale.ROOT));
          if (action != null)
            try {
              action.execute(p, pair.length == 2 ? pair[1].stripLeading() : "");
            } catch (IllegalArgumentException ex) {
              context.plugin.getLogger().warning("Invalid menu action: " + raw);
            }
        }
    }
  }

  @EventHandler
  public void drag(InventoryDragEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof Menu) e.setCancelled(true);
  }
}
