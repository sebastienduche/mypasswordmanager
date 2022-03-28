package com.passwordmanager;

import com.passwordmanager.data.PasswordData;
import com.passwordmanager.data.PasswordListData;
import com.passwordmanager.exception.DashlaneImportException;
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
import java.util.stream.Collectors;

import static com.passwordmanager.Utils.cleanString;

public class PasswordController {

    private static PasswordListData passwordListData = null;
    private static String filter = "";
    private static String masterPassword = "";

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
                .collect(Collectors.toList());
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

    public static void importDashlaneCSV(File file) throws DashlaneImportException {
        if (file == null || !file.exists()) {
            return;
        }
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedInputStream = new BufferedReader(fileReader)) {
            bufferedInputStream.lines().forEach(PasswordController::importLine);
        } catch (IOException e) {
            throw new DashlaneImportException();
        }

        passwordListData.getPasswordDataList().sort(Comparator.comparing(PasswordData::getName));
    }


    private static void importLine(String line) {
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

    public static void filterPasswords(String value) {
        if (value == null) {
            filter = "";
        } else {
            filter = value;
        }
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
