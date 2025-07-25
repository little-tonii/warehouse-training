package com.training.warehouse.service;

import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface PdfService {
    PDDocument convertImageToPdf(byte[] imageBytes);
    byte[] mergeWithBookmarks(Map<String, byte[]> files);
}
