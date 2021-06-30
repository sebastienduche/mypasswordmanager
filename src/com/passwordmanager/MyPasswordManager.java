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
  JMenuItem importFile = new JMenuItem(new ImportFileAction());
  JMenuItem exit = new JMenuItem(new ExitAction());

  PasswordTableModel model;
  JTable table;

  public MyPasswordManager() throws HeadlessException {
    PasswordController.createList();
    setTitle("MyPasswordManager");
    setSize(800, 600);
    setLayout(new BorderLayout());
    menuBar.add(menuFile);
    menuFile.add(newFile);
    menuFile.add(openFile);
    menuFile.add(saveFile);
    menuFile.addSeparator();
    menuFile.add(importFile);
    menuFile.addSeparator();
    menuFile.add(exit);
    setJMenuBar(menuBar);
    JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "grow", "grow"));
    add(panel, BorderLayout.CENTER);
    table = new JTable(model = new PasswordTableModel());
    panel.add(new JScrollPane(table), "grow");

    JToolBar toolBar = new JToolBar();
    toolBar.add(new JButton(new NewFileAction()));
    toolBar.add(new JButton(new OpenFileAction()));
    toolBar.add(new JButton(new SaveFileAction()));
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
      super("+ Password");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      PasswordController.addItem(new PasswordData());
      model.fireTableDataChanged();
    }
  }
  
  class DeleteAction extends AbstractAction {
	    public DeleteAction() {
	      super("- Password");
	    }

	    @Override
	    public void actionPerformed(ActionEvent e) {
	      PasswordController.removeItemAt(table.getSelectedRow());
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
	          PasswordController.importDashlaneCSV(nomFichier);
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
	      System.exit(0);
	    }
	  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(MyPasswordManager::new);
  }
}
