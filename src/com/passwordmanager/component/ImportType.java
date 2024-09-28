package com.passwordmanager.component;

import static com.passwordmanager.Utils.getLabel;

public enum ImportType {
  NONE(""),
  DASHLANE(getLabel("ImportType.dashlane")),
  APPLE_PASSWORD(getLabel("ImportType.apple"));
  private final String label;

  ImportType(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
