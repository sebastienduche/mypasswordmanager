package com.passwordmanager;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.List;

public class Filtre extends FileFilter {

    public static final Filtre FILTRE_SINFOS = new Filtre("sinfos", Utils.getLabel("filter.file"));
    public static final Filtre FILTRE_PDF = new Filtre("pdf", Utils.getLabel("filter.pdf"));
    public static final Filtre FILTRE_CSV = new Filtre("csv", Utils.getLabel("filter.csv"));

    private final List<String> suffixes;
    private final String description;

    public Filtre(List<String> suffixes, String description) {
        this.suffixes = suffixes.stream()
                .map(String::toLowerCase)
            .toList();
        this.description = description;
    }

    private Filtre(String suffixe, String description) {
        this(List.of(suffixe), description);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String suffixe = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            suffixe = s.substring(i + 1).toLowerCase().strip();
        }
        return suffixe != null && suffixes.contains(suffixe);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String toString() {
        return "." + suffixes.get(0);
    }

}
