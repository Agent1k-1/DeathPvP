package com.agent1k.action;

import com.agent1k.core.PvpContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ActionExecutor {
  private final PvpContext context;

  public ActionExecutor(PvpContext context) {
    this.context = context;
  }

  public void execute(
      CommandSender sender, List<String> actions, Map<String, String> placeholders) {
    String title = null;
    String subtitle = null;
    for (String raw : actions) {
      String line = raw.strip();
      String type;
      String value;
      if (line.startsWith("[") && line.contains("]")) {
        int end = line.indexOf(']');
        type = line.substring(1, end).trim();
        value = line.substring(end + 1).stripLeading();
      } else {
        String[] pair = line.split(":", 2);
        type = pair[0].trim();
        value = pair.length == 2 ? pair[1].stripLeading() : "";
      }
      type = type.toUpperCase(Locale.ROOT);
      for (var entry : placeholders.entrySet()) {
        value = value.replace("%" + entry.getKey() + "%", entry.getValue());
      }
      switch (type) {
        case "MESSAGE", "CHAT" -> sender.sendMessage(context.message.color(value));
        case "TITLE" -> title = value;
        case "SUBTITLE" -> subtitle = value;
        default -> {
          Action action = context.actions.get(type);
          if (action == null) {
            context.plugin.getLogger().warning("Неизвестное действие: " + raw);
          } else if (sender instanceof Player player) {
            try {
              action.execute(player, value);
            } catch (IllegalArgumentException exception) {
              context.plugin.getLogger().warning("Некорректное действие: " + raw);
            }
          }
        }
      }
    }
    if (sender instanceof Player player && (title != null || subtitle != null)) {
      player.sendTitle(
          context.message.color(title == null ? "" : title),
          context.message.color(subtitle == null ? "" : subtitle),
          0,
          25,
          5);
    }
  }
}
