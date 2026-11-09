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
    if (!configuration.isString("name") || !configuration.isInt("size")) {
      throw new IllegalArgumentException("Menu requires name (string) and size (integer)");
    }
    name = configuration.getString("name");
    size = configuration.getInt("size");
    if (size < 9 || size > 54 || size % 9 != 0) {
      throw new IllegalArgumentException("Menu size must be 9, 18, 27, 36, 45 or 54");
    }
    List<MenuItem> loaded = new ArrayList<>();
    ConfigurationSection settings = configuration.getConfigurationSection("menu-settings");
    if (settings != null) {
      for (String key : settings.getKeys(false)) {
        ConfigurationSection item = settings.getConfigurationSection(key);
        if (item == null || !item.isInt("slot")) continue;
        int slot = item.getInt("slot");
        if (slot < 0 || slot >= size) continue;
        if (!item.isString("material") || !item.isString("name")) {
          throw new IllegalArgumentException("Menu item " + key + " requires material and name");
        }
        Material material = Material.matchMaterial(item.getString("material"));
        if (material == null || material.isAir() || !material.isItem()) continue;
        loaded.add(
            new MenuItem(
                item.getString("name"),
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
