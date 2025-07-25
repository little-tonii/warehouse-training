package com.training.warehouse.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
            PDDocument mergedDocument = new PDDocument();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PDDocumentOutline bookmarkOutline = new PDDocumentOutline();
            List<PDDocument> documentsToClose = new ArrayList<>();
            mergedDocument.getDocumentCatalog().setDocumentOutline(bookmarkOutline);
            for (Map.Entry<String, byte[]> fileEntry : inputFiles.entrySet()) {
                String fileName = fileEntry.getKey();
                byte[] fileContent = fileEntry.getValue();
                PDDocument sourceDocument;
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    sourceDocument = Loader.loadPDF(fileContent);
                } else {
                    sourceDocument = convertImageToPdf(fileContent);
                }
                documentsToClose.add(sourceDocument);
                int startPageIndex = mergedDocument.getNumberOfPages();
                for (PDPage page : sourceDocument.getPages()) {
                    mergedDocument.importPage(page);
                }
                if (mergedDocument.getNumberOfPages() > startPageIndex) {
                    PDPageDestination destination = new PDPageFitDestination();
                    destination.setPage(mergedDocument.getPage(startPageIndex));
                    PDOutlineItem bookmarkItem = new PDOutlineItem();
                    bookmarkItem.setTitle(fileName);
                    bookmarkItem.setDestination(destination);
                    bookmarkOutline.addLast(bookmarkItem);
                }
            }
            bookmarkOutline.openNode();
            mergedDocument.save(outputStream);
            for (PDDocument document : documentsToClose) {
                document.close();
            }
            mergedDocument.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
