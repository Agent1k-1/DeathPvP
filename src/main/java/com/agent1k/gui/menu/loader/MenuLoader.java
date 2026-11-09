package com.agent1k.gui.menu.loader;

import com.agent1k.gui.config.MenuConfig;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MenuLoader {
  private final JavaPlugin plugin;
  private final Map<String, MenuConfig> menus = new HashMap<>();

  public MenuLoader(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void load() {
    File directory = new File(plugin.getDataFolder(), "menus");
    if (!directory.isDirectory() && !directory.mkdirs()) {
      throw new IllegalStateException("Cannot create menus directory");
    }
    if (!new File(directory, "search.yml").exists()) plugin.saveResource("menus/search.yml", false);
    File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files == null) throw new IllegalStateException("Cannot read menus directory");
    Map<String, MenuConfig> loaded = new HashMap<>();
    for (File file : files) {
      YamlConfiguration configuration = new YamlConfiguration();
      try {
        configuration.load(file);
        loaded.put(
            file.getName().substring(0, file.getName().length() - 4),
            new MenuConfig(configuration));
      } catch (IOException | InvalidConfigurationException | IllegalArgumentException exception) {
        plugin
            .getLogger()
            .warning("Cannot load menu " + file.getName() + ": " + exception.getMessage());
        if (menus.containsKey(file.getName().replaceFirst("\\.yml$", ""))) {
          String id = file.getName().replaceFirst("\\.yml$", "");
          loaded.put(id, menus.get(id));
        }
      }
    }
    menus.clear();
    menus.putAll(loaded);
  }

  public MenuConfig get(String name) {
    return menus.get(name);
  }
}
