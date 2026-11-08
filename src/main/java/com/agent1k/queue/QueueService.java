package com.agent1k.queue;

import com.agent1k.battle.Battle;
import com.agent1k.core.PvpContext;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class QueueService {
  private final PvpContext context;

  public QueueService(PvpContext context) {
    this.context = context;
  }

  public boolean available(Player p) {
    return p != null
        && p.isOnline()
        && !p.isDead()
        && p.getGameMode() == GameMode.SURVIVAL
        && !context.battles.containsKey(p.getUniqueId());
  }

  public void join(Player p, Mode mode) {
    UUID id = p.getUniqueId();
    if (mode == Mode.TEAM && !context.parties.containsKey(id)) {
      context.message.tell(p, "party-required");
      return;
    }
    for (UUID member : context.party.group(id, mode)) {
      Player target = Bukkit.getPlayer(member);
      if (!available(target) || context.waiting.containsKey(member)) {
        context.message.tell(p, "busy");
        return;
      }
      if (!context.armor.armor(target)) {
        context.message.tell(target, "armor");
        return;
      }
    }
    for (UUID member : context.party.group(id, mode)) {
      context.waiting.put(member, mode);
      context.message.tell(Bukkit.getPlayer(member), "queued");
    }
  }

  public void leave(UUID id) {
    Mode mode = context.waiting.get(id);
    if (mode != null)
      for (UUID member : context.party.group(id, mode)) {
        context.waiting.remove(member);
        Player p = Bukkit.getPlayer(member);
        if (p != null) context.message.tell(p, "left");
      }
  }

  public void match() {
    for (UUID id : new ArrayList<>(context.waiting.keySet()))
      if (!available(Bukkit.getPlayer(id))) leave(id);
    for (Mode mode : Mode.values()) {
      List<UUID> ids =
          context.waiting.entrySet().stream()
              .filter(e -> e.getValue() == mode)
              .map(Map.Entry::getKey)
              .toList();
      if (ids.isEmpty()) continue;
      List<UUID> a = context.party.group(ids.get(0), mode);
      UUID opponent = ids.stream().filter(id -> !a.contains(id)).findFirst().orElse(null);
      if (opponent == null) continue;
      List<UUID> b = context.party.group(opponent, mode);
      if (mode == Mode.TEAM && (a.size() != 2 || b.size() != 2)) continue;
      String arena = null;
      Location first = null, second = null;
      ConfigurationSection section = null;
      if (mode == Mode.NORMAL) {
        first = context.arena.random();
        if (first != null) second = first.clone().add(3, 0, 0);
      } else {
        ConfigurationSection root = context.arenas.getConfigurationSection("arenas");
        if (root != null)
          for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s.getBoolean("enabled")
                && !context.occupied.contains(key)
                && context.arena.safe(context.arena.point(s, "first"))
                && context.arena.safe(context.arena.point(s, "second"))
                && context.arena.point(s, "min") != null
                && context.arena.point(s, "max") != null) {
              arena = key;
              section = s;
              first = context.arena.point(s, "first");
              second = context.arena.point(s, "second");
              break;
            }
          }
      }
      if (first == null || second == null) continue;
      Battle battle = new Battle(context, a, b, mode, arena, section, first, second);
      if (arena != null) context.occupied.add(arena);
      for (UUID id : battle.all) {
        context.waiting.remove(id);
        context.battles.put(id, battle);
      }
      battle.runTaskTimer(context.plugin, 0, 20);
    }
  }
}
