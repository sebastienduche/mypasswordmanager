package com.passwordmanager;

import com.passwordmanager.component.MyPasswordLabel;
import com.passwordmanager.data.PasswordData;
import com.passwordmanager.exception.DashlaneImportException;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;
import com.passwordmanager.launcher.MyPasswordManagerServer;
import com.passwordmanager.table.ButtonCellEditor;
import com.passwordmanager.table.ButtonCellRenderer;
import com.passwordmanager.table.CheckboxCellEditor;
import com.passwordmanager.table.CheckboxCellRenderer;
import com.passwordmanager.table.PasswordTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.prefs.Preferences;

public final class MyPasswordManager extends JFrame {

  // TODO PDF Export
  public static final String INTERNAL_VERSION = "2.2";
  public static final String VERSION = "2";

  private final JMenuItem saveFile = new JMenuItem(new SaveFileAction());
  private final JMenuItem importFile = new JMenuItem(new ImportFileAction());
  private final JMenuItem addPassword = new JMenuItem(new AddAction());
  private final JMenuItem deletePassword = new JMenuItem(new DeleteAction());
  private final JMenuItem changeMasterPassword = new JMenuItem(new ChangeMasterPasswordAction());
  private final PasswordTableModel model;
  private final JTable table;

  private final MyPasswordManager instance;
  private final Preferences prefs;
  private final JButton saveButton;
  private final JButton addPasswordButton;
  private final JButton deletePasswordButton;
  private final JTextField filterTextField;
  private final JLabel labelModified;
  private final JLabel labelCount;
  private static final MyPasswordLabel INFO_LABEL = new MyPasswordLabel();

  private File openedFile = null;

  public MyPasswordManager() throws HeadlessException {
    instance = this;
    prefs = Preferences.userNodeForPackage(getClass());
    PasswordController.createList();
    setTitle("MyPasswordManager");
    setLayout(new BorderLayout());
    JMenuBar menuBar = new JMenuBar();
    JMenu menuFile = new JMenu("File");
    menuBar.add(menuFile);
    JMenu menuPassword = new JMenu("Password");
    menuBar.add(menuPassword);
    JMenu menuAbout = new JMenu("?");
    menuBar.add(menuAbout);
    JMenuItem newFile = new JMenuItem(new NewFileAction());
    menuFile.add(newFile);
    JMenuItem openFile = new JMenuItem(new OpenFileAction());
    menuFile.add(openFile);
    menuFile.addSeparator();
    menuFile.add(saveFile);
    menuFile.add(new JMenuItem(new SaveAsFileAction()));
    menuFile.addSeparator();
    menuFile.add(importFile);
    menuFile.addSeparator();
    menuFile.add(changeMasterPassword);

    final String file = prefs.get("MyPassworManager.file", "");
    if (!file.isEmpty()) {
      menuFile.addSeparator();
      JMenuItem reOpen = new JMenuItem(new ReOpenFileAction());
      reOpen.setText("-" + file);
      menuFile.add(reOpen);
    }
    menuFile.addSeparator();
    menuFile.add(new JMenuItem(new ExitAction()));
    menuPassword.add(addPassword);
    menuPassword.add(deletePassword);
    menuAbout.add(new JMenuItem(new AboutAction()));
    menuAbout.add(new JMenuItem(new SearchUpdateAction()));
    setJMenuBar(menuBar);
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "grow", "[][grow][]"));
    add(panel, BorderLayout.CENTER);
    filterTextField = new JTextField();
    filterTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        final char keyChar = e.getKeyChar();
        String value = filterTextField.getText();
        if (Character.isLetterOrDigit(keyChar)) {
          value += keyChar;
        } else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !value.isEmpty()) {
        	value = value.substring(0, value.length() - 1);
        }
        PasswordController.filterPasswords(value);
        model.fireTableDataChanged();
        labelCount.setText(Integer.toString(model.getRowCount()));
      }
    });
    labelModified = new JLabel("-");
    labelCount = new JLabel("0");
    panel.add(new JLabel("Last Modified:"), "split 4");
    panel.add(labelModified, "growx, align left");
    final JLabel labelFilter = new JLabel("Search by name or URL:");
    labelFilter.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(labelFilter, "split 2, growx, align right");
    panel.add(filterTextField, "w 200, align right, wrap");
    table = new JTable(model = new PasswordTableModel());
    table.setAutoCreateRowSorter(true);
    TableColumnModel tcm = table.getColumnModel();
    TableColumn tc = tcm.getColumn(PasswordTableModel.DEPRECATED);
    tc.setCellRenderer(new CheckboxCellRenderer());
    tc.setCellEditor(new CheckboxCellEditor());
    tc.setMinWidth(80);
    tc.setMaxWidth(80);
    tc = tcm.getColumn(PasswordTableModel.COPY_PASSWORD);
    tc.setCellRenderer(new ButtonCellRenderer("", MyPasswordImage.COPY));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    tc = tcm.getColumn(PasswordTableModel.COPY_USER);
    tc.setCellRenderer(new ButtonCellRenderer("", MyPasswordImage.COPY));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    tc = tcm.getColumn(PasswordTableModel.OPEN_URL);
    tc.setCellRenderer(new ButtonCellRenderer("", MyPasswordImage.EXPORT));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(25);
    tc.setMaxWidth(25);
    panel.add(new JScrollPane(table), "grow, wrap");
    JLabel labelTotal = new JLabel("Number of passwords: ");
    labelTotal.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(labelTotal, "growx, align right, split 2");
    panel.add(labelCount);

    JToolBar toolBar = new JToolBar();
    final JButton newButton = new JButton(new NewFileAction());
    newButton.setText("");
    toolBar.add(newButton);
    final JButton openButton = new JButton(new OpenFileAction());
    openButton.setText("");
    toolBar.add(openButton);
    saveButton = new JButton(new SaveFileAction());
    saveButton.setText("");
    toolBar.add(saveButton);
    toolBar.addSeparator();
    toolBar.add(addPasswordButton = new JButton(new AddAction()));
    toolBar.add(deletePasswordButton = new JButton(new DeleteAction()));
    toolBar.setFloatable(true);
    setFileOpened(null);
    add(toolBar, BorderLayout.NORTH);
    JPanel panelBottom = new JPanel();
    panelBottom.setLayout(new MigLayout("", "0px[grow]0px", "0px[25:25:25]0px"));
    panelBottom.add(INFO_LABEL, "center");
    INFO_LABEL.setForeground(Color.red);
    add(panelBottom, BorderLayout.SOUTH);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        new ExitAction().actionPerformed(null);
      }
    });
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(Integer.parseInt(prefs.get("MyPassworManager.x", "0")), Integer.parseInt(prefs.get("MyPassworManager.y", "0")));
    int width = Integer.parseInt(prefs.get("MyPassworManager.width", "0"));
    int height = Integer.parseInt(prefs.get("MyPassworManager.height", "0"));
    setSize(width != 0 ? width : screenSize.width, height != 0 ? height : screenSize.height);
    setVisible(true);
  }

  private void setFileOpened(File file) {
    boolean opened = (openedFile = file) != null;
    labelModified.setText(PasswordController.getLastModified());
    saveFile.setEnabled(opened);
    addPassword.setEnabled(opened);
    deletePassword.setEnabled(opened);
    importFile.setEnabled(opened);
    saveButton.setEnabled(opened);
    addPasswordButton.setEnabled(opened);
    deletePasswordButton.setEnabled(opened);
    changeMasterPassword.setEnabled(opened);
    filterTextField.setText("");
    PasswordController.filterPasswords("");
    labelCount.setText(Integer.toString(PasswordController.getPasswords().size()));
    if (file == null || file.isDirectory()) {
      setTitle("MyPasswordManager");
    } else {
      setTitle("MyPasswordManager - " + file.getAbsolutePath());
    }
  }

  private boolean requestAndValidatePassword(OpenPasswordPanel openPasswordPanel, boolean newPassword, boolean newMaster) {
    JOptionPane.showMessageDialog(instance, openPasswordPanel, "Enter the password to encode", JOptionPane.PLAIN_MESSAGE, null);
    if (openPasswordPanel.isEmptyPassword()) {
      JOptionPane.showMessageDialog(instance, "The passwords can't be empty.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (!newPassword && !PasswordController.getMasterPassword().equals(openPasswordPanel.getPassword())) {
      JOptionPane.showMessageDialog(instance, "The password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (newMaster && !PasswordController.getMasterPassword().equals(openPasswordPanel.getOldPassword())) {
      JOptionPane.showMessageDialog(instance, "The password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (openPasswordPanel.isDifferentPassword()) {
      JOptionPane.showMessageDialog(instance, "The 2 passwords don't match.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  private void save(File file, boolean newPassword) {
    final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(newPassword);
    if (!requestAndValidatePassword(openPasswordPanel, newPassword, false)) {
      return;
    }
    try {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      if (PasswordController.save(file, openPasswordPanel.getPassword())) {
        INFO_LABEL.setText("File saved.", true);
        labelModified.setText(PasswordController.getLastModified());
      } else {
        INFO_LABEL.setText("Error while saving file.", true);
      }
    } catch (InvalidContentException invalidContentException) {
      JOptionPane.showMessageDialog(instance, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
    }
    setCursor(Cursor.getDefaultCursor());
  }

  public static void setInfoLabel(String text) {
    INFO_LABEL.setText(text, true);
  }

  private static void cleanDebugFiles() {
    String sDir = System.getProperty("user.home") + File.separator + "MyPasswordManagerDebug";
    File f = new File(sDir);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime monthsAgo = LocalDateTime.now().minusMonths(2);
    String[] files = f.list((dir, name) -> {
      String date = "";
      if (name.startsWith("Debug-") && name.endsWith(".log")) {
        date = name.substring(6, name.indexOf(".log"));
      }
      if (name.startsWith("DebugFtp-") && name.endsWith(".log")) {
        date = name.substring(9, name.indexOf(".log"));
      }
      if (!date.isEmpty()) {
        String[] fields = date.split("-");
        LocalDateTime dateTime = now.withMonth(Integer.parseInt(fields[1])).withDayOfMonth(Integer.parseInt(fields[0])).withYear(Integer.parseInt(fields[2]));
        return dateTime.isBefore(monthsAgo);
      }
      return false;
    });

    if (files != null) {
      for (String file : files) {
        f = new File(sDir, file);
        f.deleteOnExit();
      }
    }
  }

  class NewFileAction extends AbstractAction {
    public NewFileAction() {
      super("New File", MyPasswordImage.NEW);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setFileOpened(new File(""));
      PasswordController.createList();
      model.fireTableDataChanged();
      labelModified.setText("-");
      labelCount.setText("0");
    }
  }

  class OpenFileAction extends AbstractAction {
    public OpenFileAction() {
      super("Open File...", MyPasswordImage.OPEN);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
      boiteFichier.addChoosableFileFilter(Filtre.FILTRE_SINFOS);
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(instance)) {
        File file = boiteFichier.getSelectedFile();
        if (file == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(false);
        JOptionPane.showMessageDialog(instance, openPasswordPanel, "Enter the password to decode", JOptionPane.PLAIN_MESSAGE, null);
        boolean loaded = true;
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          PasswordController.load(file, openPasswordPanel.getPassword());
          setFileOpened(file);
        } catch (InvalidContentException invalidContentException) {
          loaded = false;
          JOptionPane.showMessageDialog(instance, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPasswordException invalidPasswordException) {
          loaded = false;
          JOptionPane.showMessageDialog(instance, "Wrong password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (loaded) {
          prefs.put("MyPassworManager.file", file.getAbsolutePath());
        }
        model.fireTableDataChanged();
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  class ReOpenFileAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
      File file = new File(prefs.get("MyPassworManager.file", ""));
      if (!file.exists()) {
        JOptionPane.showMessageDialog(instance, "The file doesn't exist : " + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(false);
      JOptionPane.showMessageDialog(instance, openPasswordPanel, "Enter the password to decode", JOptionPane.PLAIN_MESSAGE, null);
      try {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        PasswordController.load(file, openPasswordPanel.getPassword());
        setFileOpened(file);
      } catch (InvalidContentException invalidContentException) {
        JOptionPane.showMessageDialog(instance, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
      } catch (InvalidPasswordException invalidPasswordException) {
        JOptionPane.showMessageDialog(instance, "Wrong password!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      model.fireTableDataChanged();
      setCursor(Cursor.getDefaultCursor());
    }
  }

  class SaveFileAction extends AbstractAction {
    public SaveFileAction() {
      super("Save File", MyPasswordImage.SAVE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
      boiteFichier.addChoosableFileFilter(Filtre.FILTRE_SINFOS);
      if (openedFile == null || !openedFile.exists()) {
        if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
          openedFile = boiteFichier.getSelectedFile();
          if (openedFile == null) {
            setCursor(Cursor.getDefaultCursor());
            return;
          }
          if (!openedFile.getName().toLowerCase().endsWith(Filtre.FILTRE_SINFOS.toString())) {
            openedFile = new File(openedFile.getAbsolutePath() + Filtre.FILTRE_SINFOS);
          }
        } else {
          return;
        }
      }
      save(openedFile, PasswordController.getMasterPassword().isEmpty());
    }
  }

  class SaveAsFileAction extends AbstractAction {
    public SaveAsFileAction() {
      super("Save File As...", MyPasswordImage.SAVEAS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
      boiteFichier.addChoosableFileFilter(Filtre.FILTRE_SINFOS);
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
        File file = boiteFichier.getSelectedFile();
        if (file == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        if (!file.getName().toLowerCase().endsWith(Filtre.FILTRE_SINFOS.toString())) {
          file = new File(file.getAbsolutePath() + Filtre.FILTRE_SINFOS);
        }
        save(file, true);
      }
    }
  }

  class AddAction extends AbstractAction {
    public AddAction() {
      super("Password", MyPasswordImage.ADD);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PasswordController.addItem(new PasswordData());
      PasswordController.filterPasswords(null);
      filterTextField.setText("");
      model.fireTableDataChanged();
      labelCount.setText(Integer.toString(PasswordController.getPasswords().size()));
      table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
    }
  }

  class DeleteAction extends AbstractAction {
    public DeleteAction() {
      super("Password", MyPasswordImage.DELETE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final int selectedRow = table.getSelectedRow();
      if (selectedRow < 0) {
        JOptionPane.showMessageDialog(instance, "No row selected!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      final PasswordData passwordData = PasswordController.getItemAt(selectedRow);
      if (passwordData == null) {
        return;
      }
      if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(instance, passwordData.getName() == null ? "Do you want really want to delete this line?" : "Do you really want to delete: " + passwordData.getName() + "?", "Question", JOptionPane.YES_NO_OPTION)) {
        return;
      }
      PasswordController.removeItemAt(selectedRow);
      model.fireTableDataChanged();
      labelCount.setText(Integer.toString(PasswordController.getPasswords().size()));
    }
  }

  class ImportFileAction extends AbstractAction {
    public ImportFileAction() {
      super("Import Dashlane...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(instance)) {
        File file = boiteFichier.getSelectedFile();
        if (file == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          PasswordController.importDashlaneCSV(file);
        } catch (DashlaneImportException exception) {
          exception.printStackTrace();
          JOptionPane.showMessageDialog(instance, "Error while importing Dashlane CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        model.fireTableDataChanged();
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  class ExitAction extends AbstractAction {
    public ExitAction() {
      super("Exit");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, "Do you want to quit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        prefs.put("MyPassworManager.x", "" + getLocation().x);
        prefs.put("MyPassworManager.y", "" + getLocation().y);
        prefs.put("MyPassworManager.width", "" + getSize().width);
        prefs.put("MyPassworManager.height", "" + getSize().height);
        cleanDebugFiles();
        System.exit(0);
      }
    }
  }

  class AboutAction extends AbstractAction {
    public AboutAction() {
      super("About...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new APropos().setVisible(true);
    }
  }

  class SearchUpdateAction extends AbstractAction {
    public SearchUpdateAction() {
      super("Check for update...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (MyPasswordManagerServer.getInstance().hasAvailableUpdate(INTERNAL_VERSION)) {
        JOptionPane.showMessageDialog(instance, MessageFormat.format("Version {0} is available (current version: {1}). It will be install when the program will be exited.", MyPasswordManagerServer.getInstance().getAvailableVersion(), INTERNAL_VERSION), "Information", JOptionPane.INFORMATION_MESSAGE);
      } else {
        JOptionPane.showMessageDialog(instance, "No updates available.", "Information", JOptionPane.INFORMATION_MESSAGE);
      }
    }
  }

  class ChangeMasterPasswordAction extends AbstractAction {
    public ChangeMasterPasswordAction() {
      super("Change File Password...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel();
      if (!requestAndValidatePassword(openPasswordPanel, true, true)) {
        return;
      }
      PasswordController.setMasterPassword(openPasswordPanel.getPassword());
      JOptionPane.showMessageDialog(instance, "You need to save the file to take the new password in account.", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MyPasswordManager::new);
  }
}
