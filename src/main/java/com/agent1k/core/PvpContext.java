package com.agent1k.core;

import com.agent1k.action.Action;
import com.agent1k.battle.Battle;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvpContext {
  public YamlConfiguration messages, menus, arenas;
  public final Map<UUID, UUID> parties = new HashMap<>(), invitations = new HashMap<>();
  public final Map<UUID, Mode> waiting = new LinkedHashMap<>();
  public final Map<UUID, Battle> battles = new HashMap<>();
  public final Map<UUID, Location> returns = new HashMap<>();
  public final Set<String> occupied = new HashSet<>();
  public final Map<String, Action> actions = new HashMap<>();
  public final JavaPlugin plugin;
  public final com.agent1k.configuration.ConfigurationService configuration;
  public final com.agent1k.message.MessageService message;
  public final com.agent1k.armor.ArmorService armor;
  public final com.agent1k.party.PartyService party;
  public final com.agent1k.queue.QueueService queue;
  public final com.agent1k.menu.MenuService menu;
  public final com.agent1k.command.PvpCommand command;
  public final com.agent1k.arena.ArenaService arena;
  public final com.agent1k.combat.CombatService combat;
  public final com.agent1k.listener.PvpListener listener;

  public PvpContext(JavaPlugin plugin) {
    this.plugin = plugin;
    this.configuration = new com.agent1k.configuration.ConfigurationService(this);
    this.message = new com.agent1k.message.MessageService(this);
    this.armor = new com.agent1k.armor.ArmorService(this);
    this.party = new com.agent1k.party.PartyService(this);
    this.queue = new com.agent1k.queue.QueueService(this);
    this.menu = new com.agent1k.menu.MenuService(this);
    this.command = new com.agent1k.command.PvpCommand(this);
    this.arena = new com.agent1k.arena.ArenaService(this);
    this.combat = new com.agent1k.combat.CombatService(this);
    this.listener = new com.agent1k.listener.PvpListener(this);
  }
}
