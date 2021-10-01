package com.passwordmanager;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.ResourceBundle;

public class Utils {

  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

  private static ResourceBundle labels;

  public static void copyToClipboard(String text) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection contents = new StringSelection(text);
    clipboard.setContents(contents, null);
  }

  public static void openUrl(String url) {
    String value = url.toLowerCase().strip();
    if (!value.startsWith(HTTP) && !value.startsWith(HTTPS)) {
      value = HTTP + url;
    }
    try {
      Desktop.getDesktop().browse(URI.create(value));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void initResources(Locale locale) {
    labels = ResourceBundle.getBundle("label", locale);
  }

  public static String getLabel(String s) {
    if (labels == null) {
      throw new RuntimeException("Resources not initialized!");
    }
    return labels.getString(s);
  }

}
