package com.passwordmanager;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public final class MyPasswordManager extends JFrame {

  JMenuBar menuBar = new JMenuBar();
  JMenu menuFile = new JMenu("File");
  JMenuItem newFile = new JMenuItem(new NewFileAction());
  JMenuItem openFile = new JMenuItem(new OpenFileAction());
  JMenuItem saveFile = new JMenuItem(new SaveFileAction());

  PasswordTableModel model;

  public MyPasswordManager() throws HeadlessException {
    PasswordController.createList();
    setTitle("MyPasswordManager");
    setSize(800, 600);
    setLayout(new BorderLayout());
    menuBar.add(menuFile);
    menuFile.add(newFile);
    menuFile.add(openFile);
    menuFile.add(saveFile);
    setJMenuBar(menuBar);
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "grow", "grow"));
    add(panel, BorderLayout.CENTER);
    JTable table = new JTable(model = new PasswordTableModel());
    panel.add(new JScrollPane(table), "grow");

    JToolBar toolBar = new JToolBar();
    toolBar.add(new JButton(new NewFileAction()));
    toolBar.add(new JButton(new OpenFileAction()));
    toolBar.add(new JButton(new SaveFileAction()));
    toolBar.addSeparator();
    toolBar.add(new JButton(new AddAction()));
    toolBar.setFloatable(true);
    add(toolBar, BorderLayout.NORTH);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  class NewFileAction extends AbstractAction {
    public NewFileAction() {
      super("New File");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PasswordController.createList();
    }
  }

  class OpenFileAction extends AbstractAction {
    public OpenFileAction() {
      super("Open File...");
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
          PasswordController.load(nomFichier, password);
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(null, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidPasswordException invalidPasswordException) {
          JOptionPane.showMessageDialog(null, "Wrong password", "Error", JOptionPane.ERROR_MESSAGE);
        }
        model.fireTableDataChanged();
      }
    }
  }

  class SaveFileAction extends AbstractAction {
    public SaveFileAction() {
      super("Save File");
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
          PasswordController.save(nomFichier, password);
        } catch (InvalidContentException invalidContentException) {
          JOptionPane.showMessageDialog(null, "Problem!", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  class AddAction extends AbstractAction {
    public AddAction() {
      super("+ Password");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PasswordController.addItem(new PasswordData());
      model.fireTableDataChanged();
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MyPasswordManager::new);
  }
}
