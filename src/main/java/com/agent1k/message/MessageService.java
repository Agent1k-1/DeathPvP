package com.agent1k.message;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class MessageService {
  private final PvpContext context;

  public MessageService(PvpContext context) {
    this.context = context;
  }

  public String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  public void tell(Player p, String key) {
    tell(p, key, Map.of());
  }

  public void tell(Player p, String key, Map<String, String> vars) {
    ConfigurationSection s = context.messages.getConfigurationSection(key);
    if (s == null) return;
    String title = s.getString("title", ""), subtitle = s.getString("subtitle", "");
    for (var e : vars.entrySet()) {
      title = title.replace("%" + e.getKey() + "%", e.getValue());
      subtitle = subtitle.replace("%" + e.getKey() + "%", e.getValue());
    }
    if (!title.isEmpty() || !subtitle.isEmpty())
      p.sendTitle(color(title), color(subtitle), 0, 25, 5);
    for (String type : List.of("chat", "actionbar"))
      if (s.contains(type)) {
        String value = s.getString(type, "");
        for (var e : vars.entrySet()) value = value.replace("%" + e.getKey() + "%", e.getValue());
        context.actions.get(type.toUpperCase(Locale.ROOT)).execute(p, value);
      }
  }
}
