package com.training.warehouse.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.training.warehouse.service.ExcelService;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Override
    public Workbook createWorkbook(String sheetName, List<String> headers, List<Map<String, Object>> data) {
        Workbook workbook = new XSSFWorkbook();
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
        return workbook;
    }

    @Override
    public void addPieChart(XSSFWorkbook workbook, String sheetName, Map<String, Number> chartData) {
        XSSFSheet sheet = workbook.getSheet(sheetName);
        int lastRow = sheet.getLastRowNum();
        int chartHeaderRowIndex = lastRow + 2;
        Row header = sheet.createRow(chartHeaderRowIndex);
        header.createCell(0).setCellValue("Category");
        header.createCell(1).setCellValue("Value");
        int dataStartRow = chartHeaderRowIndex + 1;
        int rowIndex = dataStartRow;
        for (Map.Entry<String, Number> entry : chartData.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }
        int dataEndRow = rowIndex - 1;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 3, dataStartRow, 10, dataStartRow + 15);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Pie Chart");
        chart.setTitleOverlay(false);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.RIGHT);
        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
                sheet, new CellRangeAddress(dataStartRow, dataEndRow, 0, 0));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
                sheet, new CellRangeAddress(dataStartRow, dataEndRow, 1, 1));
        XDDFChartData pieData = chart.createData(ChartTypes.PIE, null, null);
        XDDFChartData.Series series = pieData.addSeries(categories, values);
        series.setTitle("Distribution", null);
        chart.plot(pieData);
    }
}
