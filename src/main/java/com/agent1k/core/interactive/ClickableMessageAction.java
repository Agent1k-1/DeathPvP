package com.agent1k.core.interactive;

import com.agent1k.action.Action;
import com.agent1k.utils.HexUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public final class ClickableMessageAction implements Action {
  private static final LegacyComponentSerializer SERIALIZER =
      LegacyComponentSerializer.legacySection();

  public void execute(Player player, String value) {
    Component message = Component.empty();
    for (String button : value.split("\\s*;;\\s*")) {
      String[] fields = button.split("\\s*\\|\\s*", 3);
      if (fields.length < 2 || !fields[1].startsWith("/")) {
        throw new IllegalArgumentException("Expected text | /command | hover");
      }
      Component part =
          SERIALIZER
              .deserialize(HexUtils.color(fields[0]))
              .clickEvent(ClickEvent.runCommand(fields[1]));
      if (fields.length == 3) {
        part =
            part.hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(HexUtils.color(fields[2]))));
      }
      message = message.append(part).append(Component.text("  "));
    }
    player.sendMessage(message);
  }
}
