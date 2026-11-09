package com.agent1k.gui.config;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public final class MenuConfig {
  private final String name;
  private final int size;
  private final List<MenuItem> items;

  public MenuConfig(ConfigurationSection configuration) {
    name = configuration.getString("name", "DeathPvP");
    size = Math.max(9, Math.min(54, configuration.getInt("size", 27) / 9 * 9));
    List<MenuItem> loaded = new ArrayList<>();
    ConfigurationSection settings = configuration.getConfigurationSection("menu-settings");
    if (settings != null) {
      for (String key : settings.getKeys(false)) {
        ConfigurationSection item = settings.getConfigurationSection(key);
        if (item == null || !item.isInt("slot")) continue;
        int slot = item.getInt("slot");
        if (slot < 0 || slot >= size) continue;
        Material material = Material.matchMaterial(item.getString("material", "STONE"));
        if (material == null || material.isAir() || !material.isItem()) continue;
        loaded.add(
            new MenuItem(
                item.getString("name", " "),
                item.getStringList("lore"),
                slot,
                material,
                item.getStringList("actions")));
      }
    }
    items = List.copyOf(loaded);
  }

  public String getName() {
    return name;
  }

  public int getSize() {
    return size;
  }

  public List<MenuItem> getItems() {
    return items;
  }

  public record MenuItem(
      String name, List<String> lore, int slot, Material material, List<String> actions) {
    public MenuItem {
      lore = List.copyOf(lore);
      actions = List.copyOf(actions);
    }
  }
}
