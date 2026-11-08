package com.agent1k.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public final class HexUtils {
  private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

  private HexUtils() {}

  public static String color(String text) {
    if (text == null || text.isEmpty()) return "";
    Matcher matcher = HEX_PATTERN.matcher(text);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      StringBuilder replacement = new StringBuilder("\u00a7x");
      for (char digit : matcher.group(1).toCharArray()) {
        replacement.append('\u00a7').append(Character.toLowerCase(digit));
      }
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
    }
    matcher.appendTail(result);
    return ChatColor.translateAlternateColorCodes('&', result.toString());
  }
}
