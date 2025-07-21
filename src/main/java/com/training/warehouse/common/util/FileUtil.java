package com.training.warehouse.common.util;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

import static com.training.warehouse.service.FileStoreService.ALLOWED_CONTENT_TYPES;

public class FileUtil {
    private FileUtil(){
    }

    public static void validateFileName(String fileName){
        String name = Optional.ofNullable(fileName)
                .map(n -> Paths.get(n).getFileName().toString())
                .orElseThrow(() -> new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID));

        if (name.isBlank() || !name.matches("^[a-zA-Z0-9._-]{1,255}$")) {
            throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
        }
    }

//    public static XSSFWorkbook createSummaryWorkbook(Map<Integer, List<InboundSummaryPerMonth>> groupedData) {
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet summarySheet = workbook.createSheet("Summary");
//        int rowIdx = 0;
//
//        Row header = summarySheet.createRow(rowIdx++);
//        header.createCell(0).setCellValue("Month");
//        header.createCell(1).setCellValue("Supplier");
//        header.createCell(2).setCellValue("Aircon");
//        header.createCell(3).setCellValue("SparePart");
//
//        Set<Integer> month = new HashSet<>();
//        for (Map.Entry<Integer, List<InboundSummaryPerMonth>> partDataEntry : groupedData.entrySet()) {
//            List<InboundSummaryPerMonth> inboundSummaryPerMonthsList = partDataEntry.getValue();
//            month.add(partDataEntry.getKey());
//
//            for(InboundSummaryPerMonth inboundSummaryPerMonth : inboundSummaryPerMonthsList){
//                nboundSummaryPerMonth.getSupplierCd()
//            }
//
//            long aircon = productMap.getOrDefault(ProductType.AIRCON.getName(), 0L);
//            long spare = productMap.getOrDefault(ProductType.SPARE_PART.getName(), 0L);
//
//            Row row = summarySheet.createRow(rowIdx++);
//            row.createCell(0).setCellValue(supplierCdKey);
//            row.createCell(1).setCellValue((double) aircon);
//            row.createCell(2).setCellValue((double) spare);
//        }
//
//        return workbook;
//    }
}
