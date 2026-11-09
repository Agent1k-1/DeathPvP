package com.agent1k.interactive;

import java.util.List;
import java.util.regex.Pattern;

public final class InvitationActions {
  private static final Pattern PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");

  private InvitationActions() {}

  public static List<String> resolve(List<String> actions, String inviter) {
    return actions.stream().map(action -> resolve(action, inviter)).toList();
  }

  private static String resolve(String action, String inviter) {
    String trimmed = action.strip();
    if (!trimmed.regionMatches(true, 0, "[click]", 0, 7)) return action;
    String value = trimmed.substring(7).stripLeading();
    String[] legacy = value.split("\\s*\\|\\s*", 3);
    if (legacy.length >= 2 && legacy[1].startsWith("/")) return action;
    if (inviter == null || !PLAYER_NAME.matcher(inviter).matches()) {
      throw new IllegalArgumentException("Invitation click action requires an inviter");
    }
    String[] buttons = value.split("\\s*;\\s*", -1);
    if (buttons.length != 2) {
      throw new IllegalArgumentException("Invitation click action requires two buttons");
    }
    String[] commands = {"accept", "deny"};
    StringBuilder result = new StringBuilder("[click] ");
    for (int index = 0; index < buttons.length; index++) {
      String[] fields = buttons[index].split("\\s*\\|\\s*", 2);
      if (fields.length != 2 || fields[0].isBlank()) {
        throw new IllegalArgumentException("Expected invitation button text | hover");
      }
      if (index > 0) result.append(" ;; ");
      result
          .append(fields[0])
          .append(" | /pvp team ")
          .append(commands[index])
          .append(' ')
          .append(inviter)
          .append(" | ")
          .append(fields[1]);
    }
    return result.toString();
  }
}
