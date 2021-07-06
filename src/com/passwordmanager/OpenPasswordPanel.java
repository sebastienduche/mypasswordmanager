package com.passwordmanager;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;

public final class OpenPasswordPanel extends JPanel {

  JPasswordField passwordField = new JPasswordField();
  JPasswordField passwordRepeatField = new JPasswordField();

  public OpenPasswordPanel(boolean newPassword) {
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

  public boolean isSamePassword() {
    return Arrays.toString(passwordField.getPassword()).equals(Arrays.toString(passwordRepeatField.getPassword()));
  }
  
  public boolean isEmptyPassword() {
	return passwordField.getPassword().length == 0 || passwordRepeatField.getPassword().length == 0;
  }
}
