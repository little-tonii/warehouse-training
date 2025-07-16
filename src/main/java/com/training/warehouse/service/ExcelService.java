package com.training.warehouse.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;

public interface ExcelService {
    Workbook createWorkbook();
    void addSheetToWorkbook(Workbook workbook, String sheetName, List<String> headers, List<Map<String, Object>> data);
    byte[] writeWorkbookToBytes(Workbook workbook);
}
