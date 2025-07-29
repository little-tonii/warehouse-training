package com.training.warehouse.service.impl;

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
import com.training.warehouse.dto.response.StockProjection;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.util.*;
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

    public static XSSFWorkbook createStockSummary(StockProjection data){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet summarySheet = workbook.createSheet("Summary");
        ExcelServiceImpl.clearSheet(summarySheet);
        int rowIdx = 0;

        Row header = summarySheet.createRow(rowIdx++);
        header.createCell(0).setCellValue("Start Quantity");
        header.createCell(1).setCellValue("End Quantity");
        header.createCell(2).setCellValue("Diff Quantity");

        Row rowData =summarySheet.createRow(rowIdx);
        rowData.createCell(0).setCellValue(
                data.getStartQuantity() != null ? data.getStartQuantity() : 0.0
        );
        rowData.createCell(1).setCellValue(
                data.getEndQuantity() != null ? data.getEndQuantity() : 0.0
        );
        rowData.createCell(2).setCellValue(data.getDiffQuantity());

        return workbook;
    }

    public static void drawStockSummaryBarChart(XSSFWorkbook workbook, int col1, int row1, int col2, int row2, int month) {
        XSSFSheet sheet = workbook.getSheet("Summary");
        if (sheet == null) {
            throw new IllegalStateException("Sheet 'Summary' không tồn tại.");
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText("Stock Quantity Chart Month " + month);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        xAxis.setTitle("Time");
        XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.LEFT);
        yAxis.setTitle("Quantity");

        // Lấy dữ liệu từ sheet
        double start = 0.0;
        double end = 0.0;
        Row row = sheet.getRow(1);
        if (row != null) {
            Cell startCell = row.getCell(0);
            Cell endCell = row.getCell(1);
            start = (startCell != null && startCell.getCellType() == CellType.NUMERIC) ? startCell.getNumericCellValue() : 0.0;
            end = (endCell != null && endCell.getCellType() == CellType.NUMERIC) ? endCell.getNumericCellValue() : 0.0;
        }

        // Dữ liệu cho biểu đồ
        String[] xLabels = new String[] { "Start", "End" };
        Double[] yValues = new Double[] { start, end };

        XDDFDataSource<String> dataX = XDDFDataSourcesFactory.fromArray(xLabels);
        XDDFNumericalDataSource<Double> dataY = XDDFDataSourcesFactory.fromArray(yValues);

        XDDFChartData data = chart.createData(ChartTypes.BAR, xAxis, yAxis);
        XDDFBarChartData bar = (XDDFBarChartData) data;
        bar.setBarDirection(BarDirection.COL);

        XDDFChartData.Series series = data.addSeries(dataX, dataY);
        series.setTitle("Stock Quantity", null);

        XDDFShapeProperties dataProps = new XDDFShapeProperties();
        dataProps.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLUE)));
        series.setShapeProperties(dataProps);

        chart.plot(data);
    }

    public static void clearSheet(XSSFSheet sheet) {
        int lastRow = sheet.getLastRowNum();
        for (int i = lastRow; i >= 0; i--) {
            XSSFRow row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }
}
