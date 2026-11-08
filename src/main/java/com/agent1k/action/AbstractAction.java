package com.agent1k.action;

import com.agent1k.utils.HexUtils;

public abstract class AbstractAction implements Action {
  protected String color(String text) {
    return HexUtils.color(text);
  }
}
