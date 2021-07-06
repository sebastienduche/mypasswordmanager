package com.passwordmanager.table;

import com.passwordmanager.PasswordController;
import com.passwordmanager.PasswordData;
import com.passwordmanager.Utils;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordTableModel extends DefaultTableModel {

  public static final int COPY_USER = 2;
  public static final int COPY_PASSWORD = 4;
  public static final int OPEN_URL = 6;
  public static final int DEPRECATED = 9;

  private final List<String> columns = List.of("Name", "User", "", "Password", "",  "URL", "", "Hint", "Comment", "Deprecated");
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
      case COPY_USER: return Boolean.TRUE;
      case 3: return passwordData.getPassword();
      case COPY_PASSWORD: return Boolean.TRUE;
      case 5: return passwordData.getUrl();
      case OPEN_URL: return Boolean.TRUE;
      case 7: return passwordData.getHint();
      case 8: return passwordData.getComment();
      case DEPRECATED: return deprecatedMap.getOrDefault(row, Boolean.FALSE);
      default: return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    final PasswordData passwordData = PasswordController.getPasswords().get(row);
    switch (column) {
      case 0: passwordData.setName((String) value); break;
      case 1: passwordData.setUser((String) value); break;
      case COPY_USER: Utils.copyToClipboard(passwordData.getUser()); break;
      case 3: passwordData.setPassword((String) value); break;
      case COPY_PASSWORD: Utils.copyToClipboard(passwordData.getPassword()); break;
      case 5: passwordData.setUrl((String) value); break;
      case OPEN_URL: Utils.openUrl(passwordData.getUrl()); break;
      case 7: passwordData.setHint((String) value); break;
      case 8: passwordData.setComment((String) value); break;
      case DEPRECATED: {
        deprecatedMap.put(row, (Boolean) value);
        passwordData.setDeprecated((Boolean) value);
        break;
      }
    }
  }
}
