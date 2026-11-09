package com.agent1k.core.battle;

import com.agent1k.core.PvpContext;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class Battle extends BukkitRunnable {
  private final PvpContext context;
  public final List<UUID> a, b, all;
  public final Set<UUID> alive;
  public final Mode mode;
  public final String arena;
  public final ConfigurationSection region;
  public final Location first, second;
  public int time, tick;
  public boolean active, finished;

  public Battle(
      PvpContext context,
      List<UUID> a,
      List<UUID> b,
      Mode mode,
      String arena,
      ConfigurationSection region,
      Location first,
      Location second) {
    this.context = context;
    this.time = context.plugin.getConfig().getInt("cooldown", 5);
    this.a = a;
    this.b = b;
    this.mode = mode;
    this.arena = arena;
    this.region = region;
    this.first = first;
    this.second = second;
    all = new ArrayList<>(a);
    all.addAll(b);
    alive = new HashSet<>(all);
  }

  public void run() {
    if (finished) return;
    if (!active) {
      for (UUID id : all) {
        Player p = Bukkit.getPlayer(id);
        if (p == null || p.isDead() || !context.armor.armor(p)) {
          finish(false);
          return;
        }
      }
      if (time > 0) {
        for (UUID id : all)
          context.message.tell(
              Bukkit.getPlayer(id), "countdown", Map.of("time", String.valueOf(time)));
        time--;
        return;
      }
      for (UUID id : all) {
        Player p = Bukkit.getPlayer(id);
        context.returns.put(id, p.getLocation());
        if (!p.teleport(a.contains(id) ? first : second)) {
          finish(false);
          return;
        }
      }
      active = true;
      for (UUID id : all) {
        Player p = Bukkit.getPlayer(id);
        context.combat.combat(p, "start");
        context.message.tell(p, "start");
      }
    } else if (mode != Mode.NORMAL
        && ++tick % Math.max(1, context.plugin.getConfig().getInt("combat.refresh-seconds", 5))
            == 0)
      for (UUID id : alive) {
        Player p = Bukkit.getPlayer(id);
        if (p != null) context.combat.combat(p, "start");
      }
  }

  public boolean inside(Location l) {
    if (region == null) return true;
    Location min = context.arena.point(region, "min"), max = context.arena.point(region, "max");
    return min != null
        && max != null
        && l.getWorld().equals(min.getWorld())
        && l.getX() >= Math.min(min.getX(), max.getX())
        && l.getX() <= Math.max(min.getX(), max.getX())
        && l.getY() >= Math.min(min.getY(), max.getY())
        && l.getY() <= Math.max(min.getY(), max.getY())
        && l.getZ() >= Math.min(min.getZ(), max.getZ())
        && l.getZ() <= Math.max(min.getZ(), max.getZ());
  }

  public void eliminate(UUID id) {
    alive.remove(id);
    if (a.stream().noneMatch(alive::contains) || b.stream().noneMatch(alive::contains))
      finish(true);
  }

  public void finish(boolean victory) {
    if (finished) return;
    finished = true;
    cancel();
    if (arena != null) context.occupied.remove(arena);
    for (UUID id : all) {
      context.battles.remove(id);
      Player p = Bukkit.getPlayer(id);
      if (p != null) {
        context.combat.combat(p, "end");
        if (victory && alive.contains(id)) context.message.tell(p, "win");
        if (!victory) context.message.tell(p, "cancel");
        Location back = context.returns.get(id);
        if (back != null && !p.isDead() && (mode != Mode.NORMAL || !victory)) {
          if (p.teleport(back)) context.returns.remove(id);
        } else if (mode == Mode.NORMAL && victory && !p.isDead()) context.returns.remove(id);
      }
    }
  }
}
