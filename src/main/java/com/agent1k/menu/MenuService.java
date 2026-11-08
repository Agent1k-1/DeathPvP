package com.agent1k.menu;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class MenuService {
  private final PvpContext context;

  public MenuService(PvpContext context) {
    this.context = context;
  }

  public void open(Player p) {
    Menu holder = new Menu();
    holder.inventory =
        Bukkit.createInventory(
            holder,
            Math.max(9, Math.min(54, context.menus.getInt("size", 27) / 9 * 9)),
            context.message.color(context.menus.getString("title", "DeathPvP")));
    ConfigurationSection items = context.menus.getConfigurationSection("items");
    if (items != null)
      for (String key : items.getKeys(false)) {
        ConfigurationSection s = items.getConfigurationSection(key);
        int slot = s.getInt("slot");
        if (slot < 0 || slot >= holder.inventory.getSize()) continue;
        Material material = Material.matchMaterial(s.getString("material", "STONE"));
        if (material == null || material.isAir()) continue;
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
