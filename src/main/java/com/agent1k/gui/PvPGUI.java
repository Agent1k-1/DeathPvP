package com.agent1k.gui;

import com.agent1k.core.PvpContext;
import com.agent1k.gui.holder.MenuHolder;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
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
    YamlConfiguration menu = context.menuLoader.get(name);
    if (menu == null) {
      context.plugin.getLogger().warning("Menu not loaded: " + name);
      return;
    }
    MenuHolder holder = new MenuHolder();
    holder.inventory =
        Bukkit.createInventory(
            holder,
            Math.max(9, Math.min(54, menu.getInt("size", 27) / 9 * 9)),
            context.message.color(menu.getString("name", menu.getString("title", "DeathPvP"))));
    ConfigurationSection items =
        menu.getConfigurationSection(
            menu.isConfigurationSection("menu-settings") ? "menu-settings" : "items");
    if (items != null)
      for (String key : items.getKeys(false)) {
        ConfigurationSection s = items.getConfigurationSection(key);
        if (s == null || !s.isInt("slot")) continue;
        int slot = s.getInt("slot");
        if (slot < 0 || slot >= holder.inventory.getSize()) continue;
        Material material = Material.matchMaterial(s.getString("material", "STONE"));
        if (material == null || material.isAir() || !material.isItem()) continue;
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();
        meta.setDisplayName(context.message.color(s.getString("name", " ")));
        meta.setLore(s.getStringList("lore").stream().map(context.message::color).toList());
        item.setItemMeta(meta);
        holder.inventory.setItem(slot, item);
        holder.bindings.put(slot, s.getStringList("actions"));
      }
    p.openInventory(holder.inventory);
  }
}
