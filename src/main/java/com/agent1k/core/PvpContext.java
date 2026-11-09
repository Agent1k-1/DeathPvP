package com.agent1k.core;

import com.agent1k.action.Action;
import com.agent1k.command.PvpCommand;
import com.agent1k.configuration.ConfigurationService;
import com.agent1k.core.arena.ArenaService;
import com.agent1k.core.armor.ArmorService;
import com.agent1k.core.battle.Battle;
import com.agent1k.core.combat.CombatService;
import com.agent1k.core.party.PartyService;
import com.agent1k.core.queue.QueueService;
import com.agent1k.gui.PvPGUI;
import com.agent1k.gui.menu.loader.MenuLoader;
import com.agent1k.listener.PvpListener;
import com.agent1k.message.MessageService;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvpContext {
  public YamlConfiguration messages, arenas;
  public final Map<UUID, UUID> parties = new HashMap<>(), invitations = new HashMap<>();
  public final Map<UUID, Mode> waiting = new LinkedHashMap<>();
  public final Map<UUID, Battle> battles = new HashMap<>();
  public final Map<UUID, Location> returns = new HashMap<>();
  public final Set<String> occupied = new HashSet<>();
  public final Map<String, Action> actions = new HashMap<>();
  public final JavaPlugin plugin;
  public final ConfigurationService configuration;
  public final MessageService message;
  public final ArmorService armor;
  public final PartyService party;
  public final QueueService queue;
  public final PvPGUI menu;
  public final MenuLoader menuLoader;
  public final PvpCommand command;
  public final ArenaService arena;
  public final CombatService combat;
  public final PvpListener listener;

  public PvpContext(JavaPlugin plugin) {
    this.plugin = plugin;
    this.configuration = new ConfigurationService(this);
    this.message = new MessageService(this);
    this.armor = new ArmorService(this);
    this.party = new PartyService(this);
    this.queue = new QueueService(this);
    this.menuLoader = new MenuLoader(plugin);
    this.menu = new PvPGUI(this);
    this.command = new PvpCommand(this);
    this.arena = new ArenaService(this);
    this.combat = new CombatService(this);
    this.listener = new PvpListener(this);
  }
}
