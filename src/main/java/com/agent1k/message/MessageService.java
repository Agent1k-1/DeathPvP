package com.agent1k.message;

import com.agent1k.core.PvpContext;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public final class MessageService {
  private final PvpContext context;

  public MessageService(PvpContext context) {
    this.context = context;
  }

  public String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }

  public void tell(org.bukkit.command.CommandSender sender, String key) {
    tell(sender, key, Map.of());
  }

  public void tell(org.bukkit.command.CommandSender sender, String key, Map<String, String> vars) {
    List<String> actions = new ArrayList<>();
    if (context.messages.isList(key)) {
      actions.addAll(context.messages.getStringList(key));
    } else {
      ConfigurationSection section = context.messages.getConfigurationSection(key);
      if (section == null) return;
      if (section.isList("actions")) {
        actions.addAll(section.getStringList("actions"));
      } else {
        for (String type : List.of("chat", "title", "subtitle", "actionbar")) {
          if (section.isString(type)) {
            actions.add("[" + type + "] " + section.getString(type));
          }
        }
      }
    }
    new com.agent1k.action.ActionExecutor(context).execute(sender, actions, vars);
  }
}
