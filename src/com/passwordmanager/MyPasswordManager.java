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

public final class MyPasswordManager extends JFrame {

  private final JMenuBar menuBar = new JMenuBar();
  private final JMenu menuFile = new JMenu("File");
  private final JMenu menuPassword = new JMenu("Password");
  private final JMenuItem newFile = new JMenuItem(new NewFileAction());
  private final JMenuItem openFile = new JMenuItem(new OpenFileAction());
  private final JMenuItem saveFile = new JMenuItem(new SaveFileAction());
  private final JMenuItem importFile = new JMenuItem(new ImportFileAction());
  private final JMenuItem addPassword = new JMenuItem(new AddAction());
  private final JMenuItem deletePassword = new JMenuItem(new DeleteAction());
  private final JMenuItem exit = new JMenuItem(new ExitAction());
  private final PasswordTableModel model;
  private final JTable table;

  public MyPasswordManager() throws HeadlessException {
    PasswordController.createList();
    setTitle("MyPasswordManager");
    setSize(800, 600);
    setLayout(new BorderLayout());
    menuBar.add(menuFile);
    menuBar.add(menuPassword);
    menuFile.add(newFile);
    menuFile.add(openFile);
    menuFile.add(saveFile);
    menuFile.addSeparator();
    menuFile.add(importFile);
    menuFile.addSeparator();
    menuFile.add(exit);
    menuPassword.add(addPassword);
    menuPassword.add(deletePassword);
    setJMenuBar(menuBar);
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "grow", "grow"));
    add(panel, BorderLayout.CENTER);
    table = new JTable(model = new PasswordTableModel());
    table.setAutoCreateRowSorter(true);
    TableColumnModel tcm = table.getColumnModel();
    TableColumn tc = tcm.getColumn(6);
    tc.setCellRenderer(new CheckboxCellRenderer());
    tc.setCellEditor(new CheckboxCellEditor());
    tc.setMinWidth(80);
    tc.setMaxWidth(80);
    tc = tcm.getColumn(7);
    tc.setCellRenderer(new ButtonCellRenderer("Copy password"));
    tc.setCellEditor(new ButtonCellEditor());
    tc.setMinWidth(120);
    tc.setMaxWidth(120);
    panel.add(new JScrollPane(table), "grow");

    JToolBar toolBar = new JToolBar();
    final JButton newButton = new JButton(new NewFileAction());
    newButton.setText("");
    toolBar.add(newButton);
    final JButton openButton = new JButton(new OpenFileAction());
    openButton.setText("");
    toolBar.add(openButton);
    final JButton saveButton = new JButton(new SaveFileAction());
    saveButton.setText("");
    toolBar.add(saveButton);
    toolBar.addSeparator();
    toolBar.add(new JButton(new AddAction()));
    toolBar.add(new JButton(new DeleteAction()));
    toolBar.setFloatable(true);
    add(toolBar, BorderLayout.NORTH);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(0, 0);
    setSize(screenSize.width, screenSize.height);
    setVisible(true);
  }

  class NewFileAction extends AbstractAction {
    public NewFileAction() {
      super("New File", MyPasswordImage.NEW);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
        File nomFichier = boiteFichier.getSelectedFile();
        if (nomFichier == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        final String password = JOptionPane.showInputDialog("Enter the password to decode");
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          PasswordController.load(nomFichier, password);
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(null, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPasswordException invalidPasswordException) {
          JOptionPane.showMessageDialog(null, "Wrong password", "Error", JOptionPane.ERROR_MESSAGE);
        }
        model.fireTableDataChanged();
        setCursor(Cursor.getDefaultCursor());
      }
    }
  }

  class SaveFileAction extends AbstractAction {
    public SaveFileAction() {
      super("Save File", MyPasswordImage.SAVE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(null)) {
        File nomFichier = boiteFichier.getSelectedFile();
        if (nomFichier == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        final String password = JOptionPane.showInputDialog("Enter the password to encode");
        try {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          PasswordController.save(nomFichier, password);
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(null, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
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
        JOptionPane.showMessageDialog(null, "No row selected!", "Error", JOptionPane.ERROR_MESSAGE);
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
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
        File nomFichier = boiteFichier.getSelectedFile();
        if (nomFichier == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
          PasswordController.importDashlaneCSV(nomFichier);
        } catch (DashlaneImportException exception) {
          exception.printStackTrace();
          JOptionPane.showMessageDialog(null, "Error while importing Dashlane CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
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
      if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Do you want to quit?", "Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
        System.exit(0);
      }
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MyPasswordManager::new);
  }
}
