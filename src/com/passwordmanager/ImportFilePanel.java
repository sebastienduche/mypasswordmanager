package com.passwordmanager;

import com.passwordmanager.component.ImportType;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.passwordmanager.Utils.getLabel;

public class ImportFilePanel extends JPanel {

 private final JComboBox<ImportType> importTypeJComboBox = new JComboBox<>(ImportType.values());
 private final JTextField fileText = new JTextField();

  public ImportFilePanel() {
    setLayout(new MigLayout("", "[]10px[500::]", ""));
    add(new JLabel(getLabel("importFilePanel.title")), "span 2, center, wrap");
    add(new JLabel(getLabel("importFilePanel.selectType")));
    add(importTypeJComboBox, "grow, wrap");
    add(new JLabel(getLabel("importFilePanel.file")));
    add(fileText, "grow, split 2");
    add(new JButton(new BrowseAction()));
  }

  ImportType getSelectedType() {
    return (ImportType) importTypeJComboBox.getSelectedItem();
  }

  File getFile() {
    return new File(fileText.getText().trim());
  }

  class BrowseAction extends AbstractAction {
    public BrowseAction() {
      super(getLabel("importFilePanel.browse"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      JFileChooser boiteFichier = new JFileChooser();
      if (JFileChooser.APPROVE_OPTION == boiteFichier.showOpenDialog(null)) {
        File file = boiteFichier.getSelectedFile();
        if (file == null) {
          setCursor(Cursor.getDefaultCursor());
          return;
        }
        fileText.setText(file.getAbsolutePath());
      }
    }
  }
}
