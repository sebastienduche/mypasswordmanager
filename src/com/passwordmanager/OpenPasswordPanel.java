package com.passwordmanager;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;

public final class OpenPasswordPanel extends JPanel {

  private final JPasswordField oldPasswordField = new JPasswordField();
  private final JPasswordField passwordField = new JPasswordField();
  private final JPasswordField passwordRepeatField = new JPasswordField();
  private final boolean newPassword;

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

  public OpenPasswordPanel() {
    newPassword = true;
    setLayout(new MigLayout("", "[]10px[300::]", ""));
    add(new JLabel("Enter Old password :"));
    add(oldPasswordField, "grow, wrap");
    add(new JLabel("Enter New password :"));
    add(passwordField, "grow");
    add(new JLabel("Renter New password :"), "newline");
    add(passwordRepeatField, "grow");
  }

  public String getPassword() {
    return new String(passwordField.getPassword());
  }

  public String getOldPassword() {
    return new String(oldPasswordField.getPassword());
  }

  public boolean isDifferentPassword() {
    return newPassword && !Arrays.toString(passwordField.getPassword()).equals(Arrays.toString(passwordRepeatField.getPassword()));
  }

  public boolean isEmptyPassword() {
    if (newPassword) {
      return passwordField.getPassword().length == 0 || passwordRepeatField.getPassword().length == 0;
    }
    return passwordField.getPassword().length == 0;
  }
}
