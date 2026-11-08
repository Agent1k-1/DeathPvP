package com.agent1k.configuration;

import com.agent1k.core.PvpContext;
import java.io.File;
import java.util.*;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigurationService {
  private final PvpContext context;

  public ConfigurationService(PvpContext context) {
    this.context = context;
  }

  public void load() {
    context.plugin.reloadConfig();
    context.messages =
        YamlConfiguration.loadConfiguration(
            new File(context.plugin.getDataFolder(), "messages.yml"));
    context.menus =
        YamlConfiguration.loadConfiguration(
            new File(context.plugin.getDataFolder(), "menus/search.yml"));
    context.arenas =
        YamlConfiguration.loadConfiguration(new File(context.plugin.getDataFolder(), "arenas.yml"));
  }
}
