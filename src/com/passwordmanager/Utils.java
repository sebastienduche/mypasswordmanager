package com.passwordmanager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;

public class Utils {

  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";

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
}
