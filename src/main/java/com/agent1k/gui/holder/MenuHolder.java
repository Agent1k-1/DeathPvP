package com.agent1k.gui.holder;

import java.util.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuHolder implements InventoryHolder {
  public Inventory inventory;
  public final Map<Integer, List<String>> bindings = new HashMap<>();

  public Inventory getInventory() {
    return inventory;
  }
}
