package com.passwordmanager;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;

public final class OpenPasswordPanel extends JPanel {

  JPasswordField passwordField = new JPasswordField();
  JPasswordField passwordRepeatField = new JPasswordField();
  boolean newPassword;

  public OpenPasswordPanel(boolean newPassword) {
    this.newPassword = newPassword;
    setLayout(new MigLayout("", "[]10px[300::]", ""));
    add(new JLabel("Enter password :"));
    add(passwordField, "grow");
    if (newPassword) {
      add(new JLabel("Renter password :"), "newline");
      add(passwordRepeatField, "grow");
    }
  }

  public String getPassword() {
    return new String(passwordField.getPassword());
  }

  public boolean isDifferentPassword() {
    if (newPassword) {
      return !Arrays.toString(passwordField.getPassword()).equals(Arrays.toString(passwordRepeatField.getPassword()));
    }
    return false;
  }

  public boolean isEmptyPassword() {
    if (newPassword) {
      return passwordField.getPassword().length == 0 || passwordRepeatField.getPassword().length == 0;
    }
    return passwordField.getPassword().length == 0;
  }
}
