package com.agent1k.action;

import com.agent1k.core.PvpContext;
import com.agent1k.core.interactive.ClickableMessageAction;
import com.agent1k.model.Mode;
import org.bukkit.entity.Player;

public final class ActionRegistry {
  private final PvpContext context;

  public ActionRegistry(PvpContext context) {
    this.context = context;
  }

  public void register() {
    context.actions.put("CLICK", new ClickableMessageAction());
    context.actions.put(
        "CHAT",
        new AbstractAction() {
          public void execute(Player p, String v) {
            p.sendMessage(context.message.color(v));
          }
        });
    context.actions.put("TITLE", (p, v) -> p.sendTitle(context.message.color(v), "", 0, 30, 10));
    context.actions.put("SUBTITLE", (p, v) -> p.sendTitle("", context.message.color(v), 0, 30, 10));
    context.actions.put("ACTIONBAR", (p, v) -> p.sendActionBar(context.message.color(v)));
    context.actions.put("QUEUE", (p, v) -> context.queue.join(p, Mode.valueOf(v)));
    context.actions.put("LEAVE", (p, v) -> context.queue.leave(p.getUniqueId()));
    context.actions.put("MENU", (p, v) -> context.menu.open(p, v.trim()));
    context.actions.put("CLOSE", (p, v) -> p.closeInventory());
  }
}
