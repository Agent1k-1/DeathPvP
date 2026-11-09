package com.agent1k.gui;

import com.agent1k.core.PvpContext;
import com.agent1k.gui.config.MenuConfig;
import com.agent1k.gui.holder.MenuHolder;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PvPGUI {
  private final PvpContext context;

  public PvPGUI(PvpContext context) {
    this.context = context;
  }

  public void open(Player p) {
    open(p, "search");
  }

  public void open(Player p, String name) {
    MenuConfig menu = context.menuLoader.get(name);
    if (menu == null) {
      context.plugin.getLogger().warning("Menu not loaded: " + name);
      return;
    }
    MenuHolder holder = new MenuHolder();
    holder.inventory =
        Bukkit.createInventory(holder, menu.getSize(), context.message.color(menu.getName()));
    for (MenuConfig.MenuItem configured : menu.getItems()) {
      ItemStack item = new ItemStack(configured.material());
      var meta = item.getItemMeta();
      meta.setDisplayName(context.message.color(configured.name()));
      meta.setLore(configured.lore().stream().map(context.message::color).toList());
      item.setItemMeta(meta);
      holder.inventory.setItem(configured.slot(), item);
      holder.bindings.put(configured.slot(), configured.actions());
    }
    p.openInventory(holder.inventory);
  }
}
