package com.passwordmanager.table;

import com.passwordmanager.MyPasswordManager;
import com.passwordmanager.PasswordController;
import com.passwordmanager.data.PasswordData;
import com.passwordmanager.Utils;

import javax.swing.table.DefaultTableModel;
import java.util.List;

import static com.passwordmanager.Utils.getLabel;

public class PasswordTableModel extends DefaultTableModel {

  public static final int COPY_USER = 2;
  public static final int COPY_PASSWORD = 4;
  public static final int OPEN_URL = 6;
  public static final int DEPRECATED = 9;

  private final List<String> columns = List.of(getLabel("column.name"), getLabel("column.user"), "", getLabel("column.password"), "",  getLabel("column.url"), "", getLabel("column.hint"), getLabel("column.comment"), getLabel("column.deprecated"));

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
	  if (row >= PasswordController.getPasswords().size()) {
		  return "";
	  }
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
      case DEPRECATED: return passwordData.isDeprecated();
      default: return "";
    }
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    final PasswordData passwordData = PasswordController.getPasswords().get(row);
    switch (column) {
      case 0: passwordData.setName((String) value); break;
      case 1: passwordData.setUser((String) value); break;
      case COPY_USER: copyUser(passwordData); break;
      case 3: passwordData.setPassword((String) value); break;
      case COPY_PASSWORD: copyPassword(passwordData); break;
      case 5: passwordData.setUrl((String) value); break;
      case OPEN_URL: Utils.openUrl(passwordData.getUrl()); break;
      case 7: passwordData.setHint((String) value); break;
      case 8: passwordData.setComment((String) value); break;
      case DEPRECATED: passwordData.setDeprecated((Boolean) value); break;
    }
  }

  private void copyUser(PasswordData passwordData) {
    Utils.copyToClipboard(passwordData.getUser());
    MyPasswordManager.setInfoLabel(getLabel("userCopied"));
  }

  private void copyPassword(PasswordData passwordData) {
    Utils.copyToClipboard(passwordData.getPassword());
    MyPasswordManager.setInfoLabel(getLabel("passwordCopied"));
  }
}
