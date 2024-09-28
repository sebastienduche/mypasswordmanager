package com.passwordmanager;

import com.passwordmanager.component.MyPasswordLabel;
import com.passwordmanager.data.PasswordData;
import com.passwordmanager.exception.ApplicationImportException;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;
import com.passwordmanager.launcher.MyPasswordManagerServer;
import com.passwordmanager.table.ButtonCellEditor;
import com.passwordmanager.table.ButtonCellRenderer;
import com.passwordmanager.table.CheckboxCellEditor;
import com.passwordmanager.table.CheckboxCellRenderer;
import com.passwordmanager.table.PasswordTableModel;
import com.sebastienduche.pdf.PDFProperties;
import com.sebastienduche.pdf.PDFRow;
import com.sebastienduche.pdf.PDFTools;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
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
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import static com.passwordmanager.Utils.cleanString;
import static com.passwordmanager.Utils.getLabel;

public final class MyPasswordManager extends JFrame {

    public static final String INTERNAL_VERSION = "4.1";
    public static final String VERSION = "5";
    private static final MyPasswordLabel INFO_LABEL = new MyPasswordLabel();
    private final JMenuItem saveFile;
    private final JMenuItem importFile;
    private final JMenuItem addPassword;
    private final JMenuItem deletePassword;
    private final JMenuItem closeFile;
    private final JMenuItem changeMasterPassword;
    private final JMenuItem exportToPdf;
    private final PasswordTableModel model;
    private final JTable table;
    private final MyPasswordManager instance;
    private final Preferences prefs;
    private final JButton saveButton;
    private final JButton addPasswordButton;
    private final JButton deletePasswordButton;
    private final JButton exportToPdfButton;
    private final JTextField filterTextField;
    private final JLabel labelModified;
    private final JLabel labelCount;
    private File openedFile = null;

    public MyPasswordManager() throws HeadlessException {
        instance = this;
        prefs = Preferences.userNodeForPackage(getClass());
        String locale = prefs.get("MyPassworManager.locale", "en");
        Utils.initResources(new Locale(locale));
        saveFile = new JMenuItem(new SaveFileAction());
        importFile = new JMenuItem(new ImportFileAction());
        addPassword = new JMenuItem(new AddAction());
        deletePassword = new JMenuItem(new DeleteAction());
        changeMasterPassword = new JMenuItem(new ChangeMasterPasswordAction());
        exportToPdf = new JMenuItem(new ExportToPdfAction());
        PasswordController.createList();
        setTitle("MyPasswordManager");
        setLayout(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu(getLabel("menu.file"));
        menuBar.add(menuFile);
        JMenu menuPassword = new JMenu(getLabel("menu.password"));
        menuBar.add(menuPassword);
        JMenu menuLanguage = new JMenu(getLabel("menu.language"));
        menuBar.add(menuLanguage);
        JMenu menuAbout = new JMenu("?");
        menuBar.add(menuAbout);
        JMenuItem newFile = new JMenuItem(new NewFileAction());
        menuFile.add(newFile);
        menuFile.add(new JMenuItem(new OpenFileAction()));
        closeFile = new JMenuItem(new CloseFileAction());
        menuFile.add(closeFile);
        menuFile.addSeparator();
        menuFile.add(saveFile);
        menuFile.add(new JMenuItem(new SaveAsFileAction()));
        menuFile.addSeparator();
        menuFile.add(exportToPdf);
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
        ButtonGroup languageGroup = new ButtonGroup();
        JRadioButtonMenuItem englishMenu = new JRadioButtonMenuItem(new LanguageAction(Locale.ENGLISH));
        englishMenu.setSelected(Locale.ENGLISH.getLanguage().equals(locale));
        menuLanguage.add(englishMenu);
        languageGroup.add(englishMenu);
        JRadioButtonMenuItem frenchMenuItem = new JRadioButtonMenuItem(new LanguageAction(Locale.FRENCH));
        frenchMenuItem.setSelected(Locale.FRENCH.getLanguage().equals(locale));
        menuLanguage.add(frenchMenuItem);
        languageGroup.add(frenchMenuItem);
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
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !value.isEmpty()) {
                    value = value.substring(0, value.length() - 1);
                }
                PasswordController.filterPasswords(value);
                model.fireTableDataChanged();
                labelCount.setText(Integer.toString(model.getRowCount()));
            }
        });
        labelModified = new JLabel("-");
        labelCount = new JLabel("0");
        panel.add(new JLabel(getLabel("lastModified")), "split 4");
        panel.add(labelModified, "growx, align left");
        final JLabel labelFilter = new JLabel(getLabel("searchBy"));
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
        JLabel labelTotal = new JLabel(getLabel("totalPassword"));
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
        toolBar.add(exportToPdfButton = new JButton(new ExportToPdfAction()));
        exportToPdfButton.setText("");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyPasswordManager::new);
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
        exportToPdf.setEnabled(opened);
        exportToPdfButton.setEnabled(opened);
        closeFile.setEnabled(opened);
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
        JOptionPane.showMessageDialog(instance, openPasswordPanel, getLabel("passwordToEncode"), JOptionPane.PLAIN_MESSAGE, null);
        if (openPasswordPanel.isEmptyPassword()) {
            JOptionPane.showMessageDialog(instance, getLabel("error.emptyPassword"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!newPassword && !PasswordController.getMasterPassword().equals(openPasswordPanel.getPassword())) {
            JOptionPane.showMessageDialog(instance, getLabel("error.incorrectPassword"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (newMaster && !PasswordController.getMasterPassword().equals(openPasswordPanel.getOldPassword())) {
            JOptionPane.showMessageDialog(instance, getLabel("error.incorrectPassword"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (openPasswordPanel.isDifferentPassword()) {
            JOptionPane.showMessageDialog(instance, getLabel("error.nonMatchingPasswords"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void save(File file, boolean newPassword) {
        save(file, newPassword, null);
    }

    private void save(File file, boolean newPassword, Runnable runnable) {
        final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(newPassword);
        if (!requestAndValidatePassword(openPasswordPanel, newPassword, false)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (PasswordController.save(file, openPasswordPanel.getPassword())) {
                    INFO_LABEL.setText(getLabel("fileSaved"), true);
                    labelModified.setText(PasswordController.getLastModified());
                } else {
                    INFO_LABEL.setText(getLabel("error.savingFile"), true);
                }
            } catch (InvalidContentException invalidContentException) {
                Utils.saveError(invalidContentException);
                JOptionPane.showMessageDialog(instance, getLabel("problem"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
            }
            if (runnable != null) {
                runnable.run();
            }
            setCursor(Cursor.getDefaultCursor());
        });
    }

    private List<PDFRow> getPDFRows(List<PasswordData> passwords) {
        ArrayList<PDFRow> list = new ArrayList<>();
        for (PasswordData data : passwords) {
            final PDFRow pdfRow = new PDFRow();
            pdfRow.addCell(cleanString(data.getName()));
            pdfRow.addCell(cleanString(data.getUser()));
            pdfRow.addCell(cleanString(data.getHint()));
            list.add(pdfRow);
        }
        return list;
    }

    class NewFileAction extends AbstractAction {
        public NewFileAction() {
            super(getLabel("menu.newFile"), MyPasswordImage.NEW);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (openedFile != null && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(instance, getLabel("question.createNewFile"), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                return;
            }
            setFileOpened(new File(""));
            PasswordController.createList();
            model.fireTableDataChanged();
            labelModified.setText("-");
            labelCount.setText("0");
        }
    }

    class OpenFileAction extends AbstractAction {
        public OpenFileAction() {
            super(getLabel("menu.openFile"), MyPasswordImage.OPEN);
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
                JOptionPane.showMessageDialog(instance, openPasswordPanel, getLabel("passwordToDecode"), JOptionPane.PLAIN_MESSAGE, null);
                SwingUtilities.invokeLater(() -> {
                    boolean loaded = true;
                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        PasswordController.load(file, openPasswordPanel.getPassword());
                        setFileOpened(file);
                    } catch (InvalidContentException invalidContentException) {
                        loaded = false;
                        Utils.saveError(invalidContentException);
                        JOptionPane.showMessageDialog(instance, getLabel("problem"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                    } catch (InvalidPasswordException invalidPasswordException) {
                        loaded = false;
                        JOptionPane.showMessageDialog(instance, getLabel("error.wrongPassword"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                    }
                    if (loaded) {
                        prefs.put("MyPassworManager.file", file.getAbsolutePath());
                    }
                    model.fireTableDataChanged();
                    setCursor(Cursor.getDefaultCursor());
                });
            }
        }
    }

    class CloseFileAction extends AbstractAction {
        public CloseFileAction() {
            super(getLabel("menu.closeFile"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (openedFile != null && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, getLabel("question.saveOpenedFile"), getLabel("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                save(openedFile, PasswordController.getMasterPassword().isEmpty(), () -> {
                    PasswordController.createList();
                    setFileOpened(null);
                    model.fireTableDataChanged();
                });
            } else {
                PasswordController.createList();
                setFileOpened(null);
                model.fireTableDataChanged();
            }
            labelModified.setText("-");
            labelCount.setText("0");
        }
    }

    class ReOpenFileAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = new File(prefs.get("MyPassworManager.file", ""));
            if (!file.exists()) {
                JOptionPane.showMessageDialog(instance, MessageFormat.format(getLabel("nonExistFile"), file.getAbsolutePath()), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel(false);
            JOptionPane.showMessageDialog(instance, openPasswordPanel, getLabel("passwordToDecode"), JOptionPane.PLAIN_MESSAGE, null);
            SwingUtilities.invokeLater(() -> {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    PasswordController.load(file, openPasswordPanel.getPassword());
                    setFileOpened(file);
                } catch (InvalidContentException invalidContentException) {
                    Utils.saveError(invalidContentException);
                    JOptionPane.showMessageDialog(instance, getLabel("problem"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                } catch (InvalidPasswordException invalidPasswordException) {
                    JOptionPane.showMessageDialog(instance, getLabel("error.wrongPassword"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                }
                model.fireTableDataChanged();
                setCursor(Cursor.getDefaultCursor());
            });
        }
    }

    class SaveFileAction extends AbstractAction {
        public SaveFileAction() {
            super(getLabel("menu.saveFile"), MyPasswordImage.SAVE);
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
            super(getLabel("menu.saveFileAs"), MyPasswordImage.SAVEAS);
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
            super(getLabel("password"), MyPasswordImage.ADD);
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
            super(getLabel("password"), MyPasswordImage.DELETE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(instance, getLabel("noRowSelected"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            final PasswordData passwordData = PasswordController.getPasswords().get(selectedRow);
            if (passwordData == null) {
                return;
            }
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(instance, passwordData.getName() == null ? getLabel("question.deleteThisLine") : MessageFormat.format(getLabel("question.deletePassword"), passwordData.getName()), getLabel("question"), JOptionPane.YES_NO_OPTION)) {
                return;
            }
            PasswordController.removeItem(passwordData);
            model.fireTableDataChanged();
            labelCount.setText(Integer.toString(PasswordController.getPasswords().size()));
        }
    }

    class ImportFileAction extends AbstractAction {
        public ImportFileAction() {
            super(getLabel("menu.importDashlane"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ImportFilePanel importFilePanel = new ImportFilePanel();
            JOptionPane.showMessageDialog(instance, importFilePanel, "", JOptionPane.PLAIN_MESSAGE, null);

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    PasswordController.importCSV(importFilePanel.getSelectedType(), importFilePanel.getFile());
                } catch (ApplicationImportException exception) {
                    Utils.saveError(exception);
                    JOptionPane.showMessageDialog(instance, getLabel("error.importDashlane"), getLabel("error"), JOptionPane.ERROR_MESSAGE);
                }
                model.fireTableDataChanged();
                setCursor(Cursor.getDefaultCursor());
            });
        }
    }

    class ExportToPdfAction extends AbstractAction {
        public ExportToPdfAction() {
            super(getLabel("menu.exportToPdf"), MyPasswordImage.PDF);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser boiteFichier = new JFileChooser();
            boiteFichier.removeChoosableFileFilter(boiteFichier.getFileFilter());
            boiteFichier.addChoosableFileFilter(Filtre.FILTRE_PDF);
            File exportFile = null;
            if (JFileChooser.APPROVE_OPTION == boiteFichier.showSaveDialog(instance)) {
                exportFile = boiteFichier.getSelectedFile();
                if (exportFile == null) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                if (!exportFile.getName().toLowerCase().endsWith(Filtre.FILTRE_PDF.toString())) {
                    exportFile = new File(exportFile.getAbsolutePath() + Filtre.FILTRE_PDF);
                }
            }
            try {
                PDFProperties properties = new PDFProperties(getLabel("passwords"), 10, 10, true, true, 10);
                properties.addColumn(5, getLabel("column.name"));
                properties.addColumn(7, getLabel("column.user"));
                properties.addColumn(3, getLabel("column.hint"));
                final PDFTools pdf = new PDFTools(properties, true);
                ArrayList<PDFRow> listRows = new ArrayList<>();
                for (PasswordData data : PasswordController.getPasswords()) {
                    final PDFRow pdfRow = new PDFRow();
                    pdfRow.addCell(cleanString(data.getName()));
                    pdfRow.addCell(cleanString(data.getUser()));
                    pdfRow.addCell(cleanString(data.getHint()));
                    listRows.add(pdfRow);
                }
                pdf.writeData(listRows);
                pdf.save(exportFile);
                INFO_LABEL.setText(getLabel("fileSaved"), true);
            } catch (IOException ex) {
                INFO_LABEL.setText(getLabel("error.savingFile"), true);
            }
        }
    }

    class ExitAction extends AbstractAction {
        public ExitAction() {
            super(getLabel("menu.exit"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(instance, getLabel("question.exit"), getLabel("exit"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
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
            super(getLabel("menu.about"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new APropos().setVisible(true);
        }
    }

    class SearchUpdateAction extends AbstractAction {
        public SearchUpdateAction() {
            super(getLabel("menu.checkUpdate"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (MyPasswordManagerServer.getInstance().hasAvailableUpdate(INTERNAL_VERSION)) {
                JOptionPane.showMessageDialog(instance, MessageFormat.format(getLabel("newVersion"), MyPasswordManagerServer.getInstance().getAvailableVersion(), INTERNAL_VERSION), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(instance, getLabel("noUpdate"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    class ChangeMasterPasswordAction extends AbstractAction {
        public ChangeMasterPasswordAction() {
            super(getLabel("menu.changePassword"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final OpenPasswordPanel openPasswordPanel = new OpenPasswordPanel();
            if (!requestAndValidatePassword(openPasswordPanel, true, true)) {
                return;
            }
            PasswordController.setMasterPassword(openPasswordPanel.getPassword());
            JOptionPane.showMessageDialog(instance, getLabel("changePasswordSave"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class LanguageAction extends AbstractAction {
        private final Locale locale;

        public LanguageAction(Locale locale) {
            super(getLabel("menu." + locale.getLanguage()));
            this.locale = locale;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            prefs.put("MyPassworManager.locale", locale.getLanguage());
            JOptionPane.showMessageDialog(instance, getLabel("languageChanged"), getLabel("information"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
