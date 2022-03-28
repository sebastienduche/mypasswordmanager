package com.passwordmanager.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PDFTools {

    private static final float ROW_HEIGHT = 20f;
    private static final float CELL_MARGIN = 5f;
    private final PDDocument document;
    private final PDFProperties properties;
    private final boolean withTableHeader;
    private PDPage page;
    private PDPageContentStream content;

    public PDFTools(PDFProperties properties, boolean withTableHeader) throws IOException {
        this.properties = properties;
        this.withTableHeader = withTableHeader;
        document = new PDDocument();
        page = new PDPage();
        document.addPage(page);
        content = new PDPageContentStream(document, page);
    }

    public void drawTable(PDFPageProperties pageProperties, List<PDFRow> rows) throws IOException {
        int nbrows = rows.size();
        if (nbrows == 0) {
            return;
        }
        final int nbDatas = nbrows;
        final int cols = rows.get(0).getCellCount();
        float tableWidth = page.getMediaBox().getWidth() - pageProperties.getMarginLeft() - pageProperties.getMarginRight();
        final float unitColWidth = tableWidth / 19f;

        float y = page.getMediaBox().getHeight() - pageProperties.getStartTop();

        //draw the rows
        float nexty = y;
        int maxDrawRow = nbrows;
        if (withTableHeader) {
            maxDrawRow++;
            nbrows++;
        }
        int i;
        tableWidth = unitColWidth * properties.getTotalColumnWidth();
        for (i = 0; i <= nbrows; i++) {
            content.moveTo(pageProperties.getMarginLeft(), nexty);
            content.lineTo(pageProperties.getMarginLeft() + tableWidth, nexty);
            content.stroke();
            nexty -= ROW_HEIGHT;
            if ((nexty - pageProperties.getMarginBottom()) < 0) {
                maxDrawRow = i;
                break;
            }
        }

        //draw the columns
        float nextx = pageProperties.getMarginLeft();
        float yEnd = y - (maxDrawRow * ROW_HEIGHT);
        for (i = 0; i <= cols; i++) {
            content.moveTo(nextx, y);
            content.lineTo(nextx, yEnd);
            content.stroke();
            if (i == cols) {
                break;
            }
            nextx += (unitColWidth) * properties.getColumnWidth(i);
        }

        //now add the text
        content.setFont(pageProperties.getFont(), properties.getFontSize());

        float textx = pageProperties.getMarginLeft() + CELL_MARGIN;
        float texty = y - 15;
        i = 0;
        if (withTableHeader) {
            drawRow(pageProperties, properties.getPDFHeader(), textx, texty, unitColWidth);
            texty -= ROW_HEIGHT;
            i++;
        }
        int countDatas = 0;
        for (PDFRow r : rows) {
            if (i >= maxDrawRow) {
                break;
            }
            i++;
            countDatas++;
            drawRow(pageProperties, r, textx, texty, unitColWidth);
            texty -= ROW_HEIGHT;
        }

        if (countDatas < nbDatas) {
            rows = rows.subList(countDatas, nbDatas);
            content.close();
            page = new PDPage();
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            pageProperties.setStartTop(pageProperties.getMarginTop());
            drawTable(pageProperties, rows);
            content.close();
        }
    }

    private void drawRow(PDFPageProperties pageProperties, PDFRow row, float textx, float texty, float unitColWidth) throws IOException {
        if (row.getFont() != null) {
            content.setFont(row.getFont(), row.getFontSize());
        } else {
            content.setFont(pageProperties.getFont(), pageProperties.getFontSize());
        }
        int j = 0;
        for (String text : row.getCells()) {
            content.beginText();
            content.newLineAtOffset(textx, texty);
            content.showText(text);
            content.endText();
            textx += (unitColWidth) * properties.getColumnWidth(j);
            j++;
        }
    }

    public void addTitle(float marginTop) throws IOException {
        final String title = properties.getTitle();
        final PDType1Font font = properties.isBoldTitle() ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
        final int fontSize = properties.getTitleSize();
        final float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
        final float titleHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset((page.getMediaBox().getWidth() - titleWidth) / 2, page.getMediaBox().getHeight() - marginTop - titleHeight);
        content.showText(title);
        content.endText();
    }

    public void save(File file) throws IOException {
        content.close();
        document.save(file);
        document.close();
    }

	/*public static void main(String args[]) throws IOException, COSVisitorException {
		PDDocument doc = new PDDocument();
		PDPage page = new PDPage();
		doc.addPage( page );

		PDPageContentStream contentStream = new PDPageContentStream(doc, page);

		LinkedList<PDFRow> rows = new LinkedList<PDFRow>();
		PDFRow row = new PDFRow();
		for(int j=0; j<6; j++)
			row.addCell("Header "+j);
		rows.add(row);
		row.setFont(PDType1Font.HELVETICA_BOLD, 12);
		for(int i=0; i<50; i++) {
			row = new PDFRow();
			for(int j=0; j<6; j++)
				row.addCell("Test"+i+" "+j);
			rows.add(row);
		}

		PDFPageProperties pageProperties = new PDFPageProperties(30, 20, 20, 20, PDType1Font.HELVETICA, 12);
		pageProperties.setStartTop(100);
		PDFProperties properties = new PDFProperties("Titre", 10, 12, true, true);
		properties.addColumn(0, 2);
		properties.addColumn(1, 3);
		properties.addColumn(2, 2);
		properties.addColumn(3, 3);
		properties.addColumn(4, 2);
		properties.addColumn(5, 2);
		drawTable(doc, page, contentStream, pageProperties, properties, rows, true);
		contentStream.close();
		doc.save("test.pdf");
	}
*/
}
