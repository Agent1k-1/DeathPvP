package com.agent1k.menu;

import java.util.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class Menu implements InventoryHolder {
  public Inventory inventory;
  public final Map<Integer, List<String>> bindings = new HashMap<>();

  public Inventory getInventory() {
    return inventory;
  }
}
