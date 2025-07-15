package com.training.warehouse.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.springframework.stereotype.Service;

import com.training.warehouse.service.PdfService;

@Service
public class PdfServiceImpl implements PdfService {

    @Override
    public PDDocument convertImageToPdf(byte[] imageBytes) {
        try {
            PDDocument pdDocument = new PDDocument();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            PDPage page = new PDPage(new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight()));
            pdDocument.addPage(page);
            PDImageXObject pdImage = LosslessFactory.createFromImage(pdDocument, bufferedImage);
            PDPageContentStream contentStream = new PDPageContentStream(pdDocument, page);
            contentStream.drawImage(pdImage, 0, 0);
            contentStream.close();
            return pdDocument;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public byte[] mergeWithBookmarks(Map<String, byte[]> inputFiles) {
        try {
            // Create a new PDF document to hold all merged content
            PDDocument mergedDocument = new PDDocument();
            // Initialize the root outline (bookmark) tree
            PDDocumentOutline bookmarkOutline = new PDDocumentOutline();
            mergedDocument.getDocumentCatalog().setDocumentOutline(bookmarkOutline);
            // Iterate through each input file (filename → byte[])
            for (Map.Entry<String, byte[]> fileEntry : inputFiles.entrySet()) {
                String fileName = fileEntry.getKey(); // File name (used as bookmark title)
                byte[] fileContent = fileEntry.getValue(); // File content
                PDDocument singleDocument;
                // If it's a PDF file, load it directly using PDFBox Loader
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    singleDocument = Loader.loadPDF(fileContent);
                } else {
                    // Otherwise, assume it's an image (e.g., JPG, PNG) and convert it to a one-page
                    // PDF
                    singleDocument = convertImageToPdf(fileContent);
                }
                // Record the starting page index before appending pages
                int startPageIndex = mergedDocument.getNumberOfPages();
                // Append each page from the single document to the merged document
                for (PDPage page : singleDocument.getPages()) {
                    mergedDocument.addPage(page);
                }
                // Create a bookmark pointing to the starting page of the newly added content
                PDPageDestination destination = new PDPageFitDestination();
                destination.setPage(mergedDocument.getPage(startPageIndex));
                PDOutlineItem bookmarkItem = new PDOutlineItem();
                bookmarkItem.setTitle(fileName); // Set the title of the bookmark
                bookmarkItem.setDestination(destination); // Set where the bookmark points
                bookmarkOutline.addLast(bookmarkItem); // Add bookmark to the root outline
                singleDocument.close(); // Close the temporary document after processing
            }
            // Expand the bookmarks panel by default when opening the PDF
            bookmarkOutline.openNode();
            // Save the merged document to memory (byte array)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mergedDocument.save(outputStream);
            // Close the merged PDF document
            mergedDocument.close();
            // Return the generated PDF as byte array
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
