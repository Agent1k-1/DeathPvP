package com.agent1k.listener;

import com.agent1k.core.battle.Battle;
import com.agent1k.core.PvpContext;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PvpListener implements Listener {
  private final PvpContext context;

  public PvpListener(PvpContext context) {
    this.context = context;
  }

  @EventHandler
  public void move(PlayerMoveEvent e) {
    Battle b = context.battles.get(e.getPlayer().getUniqueId());
    if (b != null && b.active && e.getTo() != null && !b.inside(e.getTo())) e.setTo(e.getFrom());
  }

  @EventHandler
  public void teleport(PlayerTeleportEvent e) {
    Battle b = context.battles.get(e.getPlayer().getUniqueId());
    if (b != null && b.active && b.mode != Mode.NORMAL) e.setCancelled(true);
  }

  @EventHandler
  public void command(PlayerCommandPreprocessEvent e) {
    Battle b = context.battles.get(e.getPlayer().getUniqueId());
    if (b != null && b.active && b.mode != Mode.NORMAL) e.setCancelled(true);
  }

  @EventHandler
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player p) {
      Battle b = context.battles.get(p.getUniqueId());
      if (b != null && !b.active) e.setCancelled(true);
    }
  }

  @EventHandler
  public void attack(EntityDamageByEntityEvent e) {
    Entity source = e.getDamager();
    if (source instanceof Projectile projectile
        && projectile.getShooter() instanceof Entity shooter) source = shooter;
    if (source instanceof Player attacker && e.getEntity() instanceof Player victim) {
      Battle a = context.battles.get(attacker.getUniqueId()),
          b = context.battles.get(victim.getUniqueId());
      if (a != null || b != null) {
        if (a != b
            || a == null
            || !a.active
            || a.a.contains(attacker.getUniqueId()) == a.a.contains(victim.getUniqueId()))
          e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void death(PlayerDeathEvent e) {
    context.queue.leave(e.getEntity().getUniqueId());
    Battle b = context.battles.get(e.getEntity().getUniqueId());
    if (b != null) b.eliminate(e.getEntity().getUniqueId());
  }

  @EventHandler
  public void respawn(PlayerRespawnEvent e) {
    Location l = context.returns.remove(e.getPlayer().getUniqueId());
    if (l != null) e.setRespawnLocation(l);
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    UUID id = e.getPlayer().getUniqueId();
    context.queue.leave(id);
    Battle b = context.battles.get(id);
    if (b != null) b.eliminate(id);
    UUID other = context.parties.remove(id);
    if (other != null) context.parties.remove(other);
    context.invitations.remove(id);
    context.invitations.values().removeIf(id::equals);
  }

  @EventHandler
  public void login(PlayerJoinEvent e) {
    Location l = context.returns.remove(e.getPlayer().getUniqueId());
    if (l != null)
      context
          .plugin
          .getServer()
          .getScheduler()
          .runTask(context.plugin, () -> e.getPlayer().teleport(l));
  }
}
