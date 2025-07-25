package com.training.warehouse.common.util;

import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.service.impl.InboundStatisticServiceImpl;
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

        XDDFDataSource<String> countries = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(firstRow, endRow, 1, 1));
        XDDFNumericalDataSource<Double> airconData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, endRow, 2, 2));
        XDDFNumericalDataSource<Double> sparePartData = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(firstRow, endRow, 3, 3));

        XDDFChartData data = chart.createData(ChartTypes.BAR3D, xAxis, yAxis);

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
