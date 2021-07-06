package com.passwordmanager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;

public class Utils {

  public static void copyToClipboard(String text) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection contents = new StringSelection(text);
    clipboard.setContents(contents, null);
  }

  public static void openUrl(String url) {
    String value = url.toLowerCase().strip();
    if (!value.startsWith("http://")) {
      value = "http://" + url;
    }
    try {
      Desktop.getDesktop().browse(URI.create(value));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
