package com.training.warehouse.common.util;

import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.dto.response.StockProjection;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.service.impl.InboundStatisticServiceImpl;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.nio.file.Paths;
import java.util.*;


public class FileUtil {
    private FileUtil(InboundStatisticServiceImpl inboundStatisticService) {
        this.inboundStatisticService = inboundStatisticService;
    }

    private final InboundStatisticServiceImpl inboundStatisticService;

    public static void validateFileName(String fileName) {
        String name = Optional.ofNullable(fileName)
                .map(n -> Paths.get(n).getFileName().toString())
                .orElseThrow(() -> new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID));

        if (name.isBlank() || !name.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
    }

    public static XSSFWorkbook createStockSummary(StockProjection data){
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet summarySheet = workbook.createSheet("Summary");
        FileUtil.clearSheet(summarySheet);
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
    public static XSSFWorkbook createSummaryWorkbook(Map<Integer, List<InboundSummaryPerMonth>> groupedData) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet summarySheet = workbook.createSheet("Summary");
        FileUtil.clearSheet(summarySheet);
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
            Map<String, Map<String, Long>> quantityPerMonth = InboundUtil.groupDataBySupplierAndProductType(inboundSummaryPerMonthsList);

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
