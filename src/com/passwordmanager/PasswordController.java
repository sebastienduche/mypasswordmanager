package com.passwordmanager;

import com.passwordmanager.component.ImportType;
import com.passwordmanager.data.PasswordData;
import com.passwordmanager.data.PasswordListData;
import com.passwordmanager.exception.ApplicationImportException;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.passwordmanager.Utils.cleanString;
import static com.passwordmanager.Utils.removeFromString;
import static java.util.stream.Collectors.toList;

public class PasswordController {

  private static PasswordListData passwordListData = null;
  private static String filter = "";
  private static String masterPassword = "";
  private static String key = UUID.randomUUID().toString();

  private PasswordController() {
  }

  public static void createList() {
    passwordListData = new PasswordListData();
    passwordListData.setPasswordDataList(new ArrayList<>());
    masterPassword = "";
  }

  public static void load(File file, String password) throws InvalidContentException, InvalidPasswordException {
    masterPassword = password;
    passwordListData = PasswordListData.load(file, password);
  }

  public static boolean save(File file, String password) throws InvalidContentException {
    passwordListData.setLastModified(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yy hh:mm")));
    return PasswordListData.save(file, passwordListData, password);
  }

  public static List<PasswordData> getPasswords() {
    if (filter.isBlank()) {
      return passwordListData.getPasswordDataList();
    }
    return passwordListData.getPasswordDataList()
        .stream()
        .filter(PasswordController::filterPasswords)
        .collect(toList());
  }

  private static boolean filterPasswords(PasswordData passwordData) {
    return passwordData.getName() != null && (passwordData.getName().toLowerCase().contains(filter.toLowerCase()) ||
        (passwordData.getUrl() != null && passwordData.getUrl().toLowerCase().contains(filter.toLowerCase())));
  }

  public static void addItem(PasswordData passwordData) {
    passwordListData.getPasswordDataList().add(passwordData);
  }

  public static void removeItem(PasswordData passwordData) {
    passwordListData.getPasswordDataList().remove(passwordData);
  }

  public static void importCSV(ImportType type, File file) throws ApplicationImportException {
    if (file == null || !file.exists()) {
      return;
    }
    try (FileReader fileReader = new FileReader(file);
         BufferedReader bufferedInputStream = new BufferedReader(fileReader)) {
      if (type == ImportType.DASHLANE) {
        bufferedInputStream.lines().forEach(PasswordController::importDashlaneLine);
      } else if (type == ImportType.APPLE_PASSWORD) {
        // Skip the Title line
        bufferedInputStream.lines().skip(1).forEach(PasswordController::importAppleLine);
      } else {
        throw new ApplicationImportException("Unknown Program Type: " + type);
      }
    } catch (IOException e) {
      throw new ApplicationImportException();
    }

    passwordListData.getPasswordDataList().sort(Comparator.comparing(PasswordData::getName));
  }


  private static void importDashlaneLine(String line) {
    String[] values = line.split("\",\"");
    if (values.length < 7) {
      return;
    }
    PasswordData passwordData = new PasswordData();
    passwordData.setName(cleanString(values[0]));
    passwordData.setUrl(cleanString(values[1]));
    passwordData.setComment(cleanString(values[2]));
    passwordData.setUser(cleanString(values[3]));
    passwordData.setPassword(cleanString(values[5]));
    addItem(passwordData);
  }

  private static void importAppleLine(String line) {
    final String appleSeparator = ",";
    final String doubleQuote = "\"";
    String[] splitByQuote = line.split(doubleQuote);
    if (splitByQuote.length > 1) {
      // If there is at least 1 double quote, we must have modulo 2 + 1 elements
      if (splitByQuote.length % 2 == 0) {
        throw new RuntimeException("Can't process this line: " + line);
      }
      while (line.contains(key)) {
        key = UUID.randomUUID().toString();
      }
      StringBuilder newLine = new StringBuilder();
      boolean skipTheElement = true;
      for (String s : splitByQuote) {
        if (skipTheElement) {
          skipTheElement = false;
          newLine.append(s);
          continue;
        } else {
          skipTheElement = true;
        }
        if (s.contains(appleSeparator)) {
          newLine.append(cleanString(s.replaceAll(appleSeparator, key)));
        }
      }
      line = newLine.toString();

    }
    String[] values = line.split(appleSeparator);
    if (values.length < 4) {
      return;
    }
    PasswordData passwordData = new PasswordData();
    passwordData.setName(cleanString(removeFromString(values[0], key, appleSeparator)));
    passwordData.setUrl(cleanString(removeFromString(values[1], key, appleSeparator)));
    passwordData.setUser(cleanString(removeFromString(values[2], key, appleSeparator)));
    passwordData.setPassword(cleanString(removeFromString(values[3], key, appleSeparator)));
    if (values.length > 4) {
      passwordData.setComment(cleanString(removeFromString(values[4], key, appleSeparator)));
    }
    addItem(passwordData);
  }

  public static void filterPasswords(String value) {
    filter = Objects.requireNonNullElse(value, "");
  }

  public static String getLastModified() {
    return passwordListData.getLastModified() == null ? "-" : passwordListData.getLastModified();
  }

  public static void setMasterPassword(String masterPassword) {
    PasswordController.masterPassword = masterPassword;
  }

  public static String getMasterPassword() {
    return masterPassword;
  }
}
