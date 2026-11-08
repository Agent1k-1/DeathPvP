package com.agent1k.action;
import org.bukkit.ChatColor;
public abstract class AbstractAction implements Action { protected String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); } }
