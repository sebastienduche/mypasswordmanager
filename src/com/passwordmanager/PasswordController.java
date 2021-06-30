package com.passwordmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PasswordController {

  private static PasswordListData passwordListData = null;

  private PasswordController() {
  }

  public static void createList() {
    passwordListData = new PasswordListData();
    passwordListData.setPasswordDataList(new ArrayList<>());
  }

  public static boolean load(File file, String password) throws InvalidContentException, InvalidPasswordException {
    passwordListData = PasswordListData.load(file, password);
    return passwordListData != null;
  }

  public static boolean save(File file, String password) throws InvalidContentException {
    return PasswordListData.save(file, passwordListData, password);
  }

  public static List<PasswordData> getPasswords() {
    return passwordListData.getPasswordDataList();
  }

  public static void addItem(PasswordData passwordData) {
    passwordListData.getPasswordDataList().add(passwordData);
  }
}
