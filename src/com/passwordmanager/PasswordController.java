package com.passwordmanager;

import com.passwordmanager.exception.DashlaneImportException;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordController {

	private static PasswordListData passwordListData = null;
	private static String filter = "";

	private PasswordController() {
	}

	public static void createList() {
		passwordListData = new PasswordListData();
		passwordListData.setPasswordDataList(new ArrayList<>());
	}

	public static void load(File file, String password) throws InvalidContentException, InvalidPasswordException {
		passwordListData = PasswordListData.load(file, password);
	}

	public static boolean save(File file, String password) throws InvalidContentException {
		return PasswordListData.save(file, passwordListData, password);
	}

	public static List<PasswordData> getPasswords() {
		if (filter.isBlank()) {
			return passwordListData.getPasswordDataList();
		}
		return passwordListData.getPasswordDataList()
				.stream()
				.filter(passwordData -> passwordData.getName().toLowerCase().contains(filter.toLowerCase()))
				.collect(Collectors.toList());
	}

	public static void addItem(PasswordData passwordData) {
		passwordListData.getPasswordDataList().add(passwordData);
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

	private static String cleanString(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		if (value.startsWith("\"")) {
			value = value.substring(1);
		}
		if (value.endsWith("\"")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	public static void removeItemAt(int row) {
		if (row == -1) {
			return;
		}
		passwordListData.getPasswordDataList().remove(row);
	}

	public static PasswordData getItemAt(int row) {
		if (row == -1) {
			return null;
		}
		return passwordListData.getPasswordDataList().get(row);
	}

	public static void filterPasswords(String value) {
		if (value == null) {
			filter = "";
		} else {
			filter = value;
		}
	}
}
