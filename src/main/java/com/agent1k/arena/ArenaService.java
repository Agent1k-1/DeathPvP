package com.agent1k.arena;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class ArenaService {
  private final PvpContext context;

  public ArenaService(PvpContext context) {
    this.context = context;
  }

  public Location point(ConfigurationSection s, String key) {
    List<Double> v = s.getDoubleList(key);
    World world = Bukkit.getWorld(s.getString("world", "world"));
    return world == null || v.size() != 3
        ? null
        : new Location(world, v.get(0), v.get(1), v.get(2));
  }

  public boolean safe(Location l) {
    return l != null
        && l.getWorld().getWorldBorder().isInside(l)
        && l.getBlock().isPassable()
        && l.clone().add(0, 1, 0).getBlock().isPassable()
        && l.clone().subtract(0, 1, 0).getBlock().getType().isSolid()
        && !Set.of(Material.MAGMA_BLOCK, Material.CACTUS, Material.CAMPFIRE, Material.SOUL_CAMPFIRE)
            .contains(l.clone().subtract(0, 1, 0).getBlock().getType());
  }

  public Location random() {
    World w = Bukkit.getWorld(context.plugin.getConfig().getString("random.world", "world"));
    if (w == null) return null;
    int radius =
        Math.max(10, Math.min(100000, context.plugin.getConfig().getInt("random.radius", 1000)));
    for (int i = 0;
        i < Math.min(256, context.plugin.getConfig().getInt("random.attempts", 64));
        i++) {
      int x = new Random().nextInt(radius * 2) - radius,
          z = new Random().nextInt(radius * 2) - radius;
      Location l = new Location(w, x + .5, w.getHighestBlockYAt(x, z) + 1, z + .5);
      if (safe(l) && safe(l.clone().add(3, 0, 0))) return l;
    }
    return null;
  }
}
