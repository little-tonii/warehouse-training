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
import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.enumeric.ProductType;

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

    public static XSSFWorkbook createSummaryWorkbook(Map<Integer, List<InboundSummaryPerMonth>> groupedData) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet summarySheet = workbook.createSheet("Summary");
        ExcelServiceImpl.clearSheet(summarySheet);
        int rowIdx = 0;

        Row header = summarySheet.createRow(rowIdx++);
        header.createCell(0).setCellValue("Month");
        header.createCell(1).setCellValue("Supplier");
        header.createCell(2).setCellValue("Aircon");
        header.createCell(3).setCellValue("SparePart");

        Set<Integer> month = new HashSet<>();
        for (Map.Entry<Integer, List<InboundSummaryPerMonth>> partDataEntry : groupedData.entrySet()) {
            List<InboundSummaryPerMonth> inboundSummaryPerMonthsList = partDataEntry.getValue();

            int monthKey = partDataEntry.getKey();
            Map<String, Map<String, Long>> quantityPerMonth = InboundStatisticServiceImpl.groupDataBySupplierAndProductType(inboundSummaryPerMonthsList);

            for (Map.Entry<String, Map<String, Long>> supplierEntry : quantityPerMonth.entrySet()) {
                Map<String, Long> productMap = supplierEntry.getValue();
                String supplierCdKey = supplierEntry.getKey();
                Row row = summarySheet.createRow(rowIdx++);
                if (!month.contains(monthKey)) {
                    month.add(monthKey);
                    row.createCell(0).setCellValue(monthKey);
                }

                long quantityAircon = productMap.getOrDefault(ProductType.AIRCON.getName(), 0L);
                long quantitySpare = productMap.getOrDefault(ProductType.SPARE_PART.getName(), 0L);

                row.createCell(1).setCellValue(supplierCdKey);
                row.createCell(2).setCellValue((double) quantityAircon);
                row.createCell(3).setCellValue((double) quantitySpare);
            }
        }
        return workbook;
    }

    public static void drawBarChart(Workbook workbook,int firstRow, int endRow,int col1, int row1,int col2,int row2, int month){
        XSSFSheet sheet = (XSSFSheet) workbook.getSheet("Summary");
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        chart.setTitleText("Quantity Chart month "+ month);
        chart.setTitleOverlay(false);

        XDDFCategoryAxis xAxis = chart.createCategoryAxis(AxisPosition.LEFT);
        xAxis.setTitle("Country");
        chart.setTitleOverlay(false);

        XDDFValueAxis yAxis = chart.createValueAxis(AxisPosition.BOTTOM);
        yAxis.setTitle("Quantity");

        // Tạo cột ảo và thêm dữ liệu
        List<String> extendedCountries = new ArrayList<>();
        List<Double> extendedAirconData = new ArrayList<>();
        List<Double> extendedSparePartData = new ArrayList<>();

        extendedCountries.add("");
        extendedAirconData.add(0.0);
        extendedSparePartData.add(0.0);

        for (int i = firstRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String country = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
                Double aircon = row.getCell(2) != null ? row.getCell(2).getNumericCellValue() : 0.0;
                Double sparePart = row.getCell(3) != null ? row.getCell(3).getNumericCellValue() : 0.0;
                extendedCountries.add(country);
                extendedAirconData.add(aircon);
                extendedSparePartData.add(sparePart);
            }
        }

        extendedCountries.add("");
        extendedAirconData.add(0.0);
        extendedSparePartData.add(0.0);

        XDDFDataSource<String> countries = XDDFDataSourcesFactory.fromArray(extendedCountries.toArray(new String[0]));
        XDDFNumericalDataSource<Double> airconData = XDDFDataSourcesFactory.fromArray(extendedAirconData.toArray(new Double[0]));
        XDDFNumericalDataSource<Double> sparePartData = XDDFDataSourcesFactory.fromArray(extendedSparePartData.toArray(new Double[0]));
        //  //

        XDDFChartData data = chart.createData(ChartTypes.BAR, xAxis, yAxis);
        // cột đứng
        XDDFBarChartData bar = (XDDFBarChartData) data;
        bar.setBarDirection(BarDirection.COL);
        bar.setBarGrouping(BarGrouping.CLUSTERED);

        XDDFChartData.Series airconSeries = data.addSeries(countries, airconData);
        XDDFChartData.Series sparePartSeries = data.addSeries(countries, sparePartData);

        airconSeries.setTitle("Aircon", null);
        sparePartSeries.setTitle("Spare Part", null);

        XDDFShapeProperties airconProps = new XDDFShapeProperties();
        airconProps.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.BLUE)));
        airconSeries.setShapeProperties(airconProps);;

        XDDFShapeProperties spareProps = new XDDFShapeProperties();
        spareProps.setFillProperties(new XDDFSolidFillProperties(XDDFColor.from(PresetColor.ORANGE)));
        sparePartSeries.setShapeProperties(spareProps);

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
