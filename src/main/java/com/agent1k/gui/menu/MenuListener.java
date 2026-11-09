package com.agent1k.gui.menu;

import com.agent1k.action.ActionExecutor;
import com.agent1k.core.PvpContext;
import com.agent1k.gui.holder.MenuHolder;
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
    if (e.getView().getTopInventory().getHolder() instanceof MenuHolder m) {
      e.setCancelled(true);
      if (e.getWhoClicked() instanceof Player p
          && e.getRawSlot() >= 0
          && e.getRawSlot() < m.inventory.getSize())
        new ActionExecutor(context)
            .execute(p, m.bindings.getOrDefault(e.getRawSlot(), List.of()), Map.of());
    }
  }

  @EventHandler
  public void drag(InventoryDragEvent e) {
    if (e.getView().getTopInventory().getHolder() instanceof MenuHolder) e.setCancelled(true);
  }
}
