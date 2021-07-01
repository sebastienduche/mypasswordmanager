package com.passwordmanager;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordTableModel extends DefaultTableModel {

  private final List<String> columns = List.of("Name", "User", "Password", "URL", "Hint", "Comment", "Deprecated");
  private final Map<Integer, Boolean> deprecatedMap = new HashMap<>();

  @Override
  public int getRowCount() {
    return PasswordController.getPasswords().size();
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  @Override
  public String getColumnName(int column) {
    return columns.get(column);
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return true;
  }

  @Override
  public Object getValueAt(int row, int column) {
    final PasswordData passwordData = PasswordController.getPasswords().get(row);
    switch (column) {
      case 0: return passwordData.getName();
      case 1: return passwordData.getUser();
      case 2: return passwordData.getPassword();
      case 3: return passwordData.getUrl();
      case 4: return passwordData.getHint();
      case 5: return passwordData.getComment();
      case 6: return deprecatedMap.getOrDefault(row, Boolean.FALSE);
      default: return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    final PasswordData passwordData = PasswordController.getPasswords().get(row);
    switch (column) {
      case 0: passwordData.setName((String) value); break;
      case 1: passwordData.setUser((String) value); break;
      case 2: passwordData.setPassword((String) value); break;
      case 3: passwordData.setUrl((String) value); break;
      case 4: passwordData.setHint((String) value); break;
      case 5: passwordData.setComment((String) value); break;
      case 6: {
        deprecatedMap.put(row, (Boolean) value);
        passwordData.setDeprecated((Boolean) value);
        break;
      }
    }
  }
}
