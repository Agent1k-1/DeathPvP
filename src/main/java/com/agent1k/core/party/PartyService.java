package com.agent1k.core.party;

import com.agent1k.core.PvpContext;
import com.agent1k.model.Mode;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PartyService {
  private final PvpContext context;

  public PartyService(PvpContext context) {
    this.context = context;
  }

  public List<UUID> group(UUID id, Mode mode) {
    return mode == Mode.TEAM && context.parties.containsKey(id)
        ? List.of(id, context.parties.get(id))
        : List.of(id);
  }

  public boolean handle(Player p, String[] args) {
    UUID id = p.getUniqueId();
    if (args[1].equalsIgnoreCase("leave")) {
      context.queue.leave(id);
      UUID other = context.parties.remove(id);
      if (other != null) context.parties.remove(other);
      return true;
    }
    if (args.length == 3) {
      Player target = Bukkit.getPlayerExact(args[2]);
      if (args[1].equalsIgnoreCase("deny")) {
        if (target == null || !target.getUniqueId().equals(context.invitations.get(id))) {
          context.message.tell(p, "invite-missing");
          return true;
        }
        context.invitations.remove(id);
        context.message.tell(p, "invite-denied");
        context.message.tell(target, "invite-rejected", Map.of("player", p.getName()));
        return true;
      }
      if (args[1].equalsIgnoreCase("accept")
          && (target == null || !target.getUniqueId().equals(context.invitations.get(id)))) {
        context.message.tell(p, "invite-missing");
        return true;
      }

      if (!context.queue.available(p)
          || !context.queue.available(target)
          || target == p
          || context.waiting.containsKey(id)
          || context.waiting.containsKey(target.getUniqueId())
          || context.parties.containsKey(id)
          || context.parties.containsKey(target.getUniqueId())) {
        context.message.tell(p, "busy");
        return true;
      }
      if (args[1].equalsIgnoreCase("add")) {
        context.invitations.put(target.getUniqueId(), id);
        context.message.tell(target, "invite", Map.of("player", p.getName()));
      }
      if (args[1].equalsIgnoreCase("accept")
          && target.getUniqueId().equals(context.invitations.get(id))) {
        context.invitations.remove(id);
        context.parties.put(id, target.getUniqueId());
        context.parties.put(target.getUniqueId(), id);
        context.message.tell(p, "team");
        context.message.tell(target, "team");
      }
    }
    return true;
  }
}
