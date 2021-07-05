package com.passwordmanager.table;

import com.passwordmanager.PasswordController;
import com.passwordmanager.PasswordData;
import com.passwordmanager.Utils;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordTableModel extends DefaultTableModel {
	
	public final static int COPY_USER = 2;
	public final static int COPY_PASSWORD = 4;
	public final static int DEPRECATED = 8;

  private final List<String> columns = List.of("Name", "User", "", "Password", "",  "URL", "Hint", "Comment", "Deprecated");
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
      case 3: return passwordData.getPassword();
      case 5: return passwordData.getUrl();
      case 6: return passwordData.getHint();
      case 7: return passwordData.getComment();
      case DEPRECATED: return deprecatedMap.getOrDefault(row, Boolean.FALSE);
      case COPY_PASSWORD: return Boolean.TRUE;
      case COPY_USER: return Boolean.TRUE;
      default: return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    final PasswordData passwordData = PasswordController.getPasswords().get(row);
    switch (column) {
      case 0: passwordData.setName((String) value); break;
      case 1: passwordData.setUser((String) value); break;
      case 3: passwordData.setPassword((String) value); break;
      case 5: passwordData.setUrl((String) value); break;
      case 6: passwordData.setHint((String) value); break;
      case 7: passwordData.setComment((String) value); break;
      case DEPRECATED: {
        deprecatedMap.put(row, (Boolean) value);
        passwordData.setDeprecated((Boolean) value);
        break;
      }
      case COPY_PASSWORD: Utils.copyToClipboard(passwordData.getPassword()); break;
      case COPY_USER: Utils.copyToClipboard(passwordData.getUser()); break;
    }
  }
}
