package com.training.warehouse.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface ExcelService {
    Workbook createWorkbook(String sheetName, List<String> headers, List<Map<String, Object>> data);
    void addPieChart(XSSFWorkbook workbook, String sheetName, Map<String, Number> data);
}
