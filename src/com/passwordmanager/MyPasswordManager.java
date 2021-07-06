package com.passwordmanager;

import com.passwordmanager.exception.DashlaneImportException;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;
import com.passwordmanager.table.ButtonCellEditor;
import com.passwordmanager.table.ButtonCellRenderer;
import com.passwordmanager.table.CheckboxCellEditor;
import com.passwordmanager.table.CheckboxCellRenderer;
import com.passwordmanager.table.PasswordTableModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

public final class MyPasswordManager extends JFrame {

  private final JMenuItem newFile = new JMenuItem(new NewFileAction());
  private final JMenuItem openFile = new JMenuItem(new OpenFileAction());
  private final JMenuItem saveFile = new JMenuItem(new SaveFileAction());
  private final JMenuItem importFile = new JMenuItem(new ImportFileAction());
  private final JMenuItem addPassword = new JMenuItem(new AddAction());
  private final JMenuItem deletePassword = new JMenuItem(new DeleteAction());
  private final PasswordTableModel model;
  private final JTable table;

  private final MyPasswordManager instance;
  private final Preferences prefs;
  private final JButton saveButton;
  private final JButton addPasswordButton;
  private final JButton deletePasswordButton;

  private File openedFile = null;

  public MyPasswordManager() throws HeadlessException {
    instance = this;
    prefs = Preferences.userNodeForPackage(getClass());
    PasswordController.createList();
    setTitle("MyPasswordManager");
    setSize(800, 600);
    setLayout(new BorderLayout());
    JMenuBar menuBar = new JMenuBar();
    JMenu menuFile = new JMenu("File");
    menuBar.add(menuFile);
    JMenu menuPassword = new JMenu("Password");
    menuBar.add(menuPassword);
    menuFile.add(newFile);
    menuFile.add(openFile);
    menuFile.addSeparator();
    menuFile.add(saveFile);
    menuFile.add(new JMenuItem(new SaveAsFileAction()));
    menuFile.addSeparator();
    menuFile.add(importFile);

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
    setJMenuBar(menuBar);
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "grow", "grow"));
    add(panel, BorderLayout.CENTER);
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
    panel.add(new JScrollPane(table), "grow");

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
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(Integer.parseInt(prefs.get("MyPassworManager.x", "0")), Integer.parseInt(prefs.get("MyPassworManager.y", "0")));
    int width = Integer.parseInt(prefs.get("MyPassworManager.width", "0"));
    int height = Integer.parseInt(prefs.get("MyPassworManager.height", "0"));
    setSize(width != 0 ? width : screenSize.width, height != 0 ? height : screenSize.height);
    setVisible(true);
  }

  private void setFileOpened(File file) {
    boolean opened = (openedFile = file) != null;
    saveFile.setEnabled(opened);
    addPassword.setEnabled(opened);
    deletePassword.setEnabled(opened);
    importFile.setEnabled(opened);
    saveButton.setEnabled(opened);
    addPasswordButton.setEnabled(opened);
    deletePasswordButton.setEnabled(opened);
    if (file == null || file.isDirectory()) {
      setTitle("MyPasswordManager");
    } else {
      setTitle("MyPasswordManager - " + file.getAbsolutePath());
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
    }
  }

  class OpenFileAction extends AbstractAction {
    public OpenFileAction() {
      super("Open File...", MyPasswordImage.OPEN);
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
          JOptionPane.showMessageDialog(instance, "Wrong password", "Error", JOptionPane.ERROR_MESSAGE);
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
        JOptionPane.showMessageDialog(instance, "Wrong password", "Error", JOptionPane.ERROR_MESSAGE);
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
      if (openedFile == null || !openedFile.exists()) {
        if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
          openedFile = boiteFichier.getSelectedFile();
          if (openedFile == null) {
            setCursor(Cursor.getDefaultCursor());
            return;
          }
        } else {
          return;
        }
        final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(true);
        JOptionPane.showMessageDialog(instance, openPasswordPanel, "Enter the password to encode", JOptionPane.PLAIN_MESSAGE, null);
        if (!openPasswordPanel.isSamePassword()) {
          JOptionPane.showMessageDialog(instance, "The 2 passwords don't match.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          PasswordController.save(openedFile, openPasswordPanel.getPassword());
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(instance, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  class SaveAsFileAction extends AbstractAction {
    public SaveAsFileAction() {
      super("Save File As...", MyPasswordImage.SAVEAS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
        File file = boiteFichier.getSelectedFile();
        if (file == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(true);
        JOptionPane.showMessageDialog(instance, openPasswordPanel, "Enter the password to encode", JOptionPane.PLAIN_MESSAGE, null);
        if (!openPasswordPanel.isSamePassword()) {
          JOptionPane.showMessageDialog(instance, "The 2 passwords don't match.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          PasswordController.save(file, openPasswordPanel.getPassword());
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(instance, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        setCursor(Cursor.getDefaultCursor());
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
      model.fireTableDataChanged();
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
      if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(instance, "Do you really want to delete: " + passwordData.getName() + "?", "Question", JOptionPane.YES_NO_OPTION)) {
        return;
      }
      PasswordController.removeItemAt(selectedRow);
      model.fireTableDataChanged();
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
        System.exit(0);
      }
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MyPasswordManager::new);
  }
}
