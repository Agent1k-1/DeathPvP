package com.agent1k;

import com.agent1k.action.ActionRegistry;
import com.agent1k.core.PvpContext;
import com.agent1k.core.battle.Battle;
import com.agent1k.gui.menu.MenuListener;
import java.io.File;
import java.util.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathPvP extends JavaPlugin {
  private PvpContext context;

  public void onEnable() {
    saveDefaultConfig();
    for (String name : List.of("messages.yml", "arenas.yml"))
      if (!new File(getDataFolder(), name).exists()) saveResource(name, false);
    context = new PvpContext(this);
    context.configuration.load();
    new ActionRegistry(context).register();
    getCommand("pvp").setExecutor(context.command);
    getCommand("deathpvp").setExecutor(context.command);
    getServer().getPluginManager().registerEvents(context.listener, this);
    getServer().getPluginManager().registerEvents(new MenuListener(context), this);
    context.plugin.getServer().getScheduler().runTaskTimer(this, context.queue::match, 20, 20);
  }

  public void onDisable() {
    if (context == null) return;
    for (Battle battle : new HashSet<>(context.battles.values())) battle.finish(false);
    context.waiting.clear();
  }
}
