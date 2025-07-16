package com.training.warehouse.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.training.warehouse.service.ExcelService;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Override
    public Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    public void addSheetToWorkbook(Workbook workbook, String sheetName, List<String> headers,
            List<Map<String, Object>> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = rowData.get(headers.get(i));
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                } else {
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }
        }
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @Override
    public byte[] writeWorkbookToBytes(Workbook workbook) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
