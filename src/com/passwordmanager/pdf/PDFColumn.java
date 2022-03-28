package com.passwordmanager.pdf;

/**
 * <p>Titre : Cave à vin</p>
 * <p>Description : Votre description</p>
 * <p>Copyright : Copyright (c) 2016</p>
 * <p>Société : Seb Informatique</p>
 *
 * @author Sébastien Duché
 * @version 0.4
 * @since 16/04/21
 */

public class PDFColumn {

    private int index;
    private int width;
    private String title;
    private String field;

    PDFColumn(String field, int index, int width, String title) {
        this.index = index;
        this.width = width;
        setField(field);
        setTitle(title);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getField() {
        return field;
    }

    private void setField(String field) {
        this.field = field;
    }
}
