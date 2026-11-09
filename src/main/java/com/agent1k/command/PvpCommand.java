package com.agent1k.command;

import com.agent1k.core.PvpContext;
import com.agent1k.core.battle.Battle;
import java.util.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PvpCommand implements CommandExecutor {
  private final PvpContext context;

  public PvpCommand(PvpContext context) {
    this.context = context;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("deathpvp")) {
      if (!sender.hasPermission("deathpvp.admin")) return true;
      if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
        for (Battle b : new HashSet<>(context.battles.values())) b.finish(false);
        context.waiting.clear();
        context.configuration.load();
        context.message.tell(sender, "reload");
      }
      return true;
    }
    if (!(sender instanceof Player p)) return true;
    if (args.length == 0) {
      context.menu.open(p);
      return true;
    }
    if (args[0].equalsIgnoreCase("leave")) {
      context.queue.leave(p.getUniqueId());
      return true;
    }
    if (args.length >= 2 && args[0].equalsIgnoreCase("team")) {
      return context.party.handle(p, args);
    }
    return true;
  }
}
