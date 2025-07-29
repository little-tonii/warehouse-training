package com.training.warehouse.controller;

import com.training.warehouse.service.InboundStatisticService;
import com.training.warehouse.service.impl.ExcelServiceImpl;
import com.training.warehouse.service.impl.InboundStatisticServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.InboundService;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import com.training.warehouse.exception.BadRequestException;


import jakarta.validation.constraints.Max;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


import java.io.ByteArrayOutputStream;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.Min;
@Controller
@RequestMapping("/api/inbound")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "inbound")
@Validated
public class InboundController {
    private final InboundService inboundService;
    private final InboundStatisticService inboundStatisticService;

    @io.swagger.v3.oas.annotations.Operation(
        method = "DELETE",
        summary = "delete inbound by id",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
        },
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "id",
                in = ParameterIn.PATH,
                required = true,
                schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "inbound deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "invalid request data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExceptionResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExceptionResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "inbound not found",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExceptionResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "internal server error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExceptionResponse.class)
                )
            )
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable @Min(value = 1, message = "inboundId must be greater than 0") long id) {
        inboundService.deleteInboundById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            method = "GET",
            summary = "get inbound summary group by productType and supplierCd",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Get inbound summary success",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = InboundSummaryResponse.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request – Invalid ID format or logic conflict",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized – Missing or invalid token",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden – Not allowed to delete this record",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found – Inbound record does not exist",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error – Unexpected issue",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    )
            }

    )
    @GetMapping("/inbound-summary")
    public Page<InboundSummaryResponse> getInboundSummary(@ParameterObject Pageable pageable) {
        return inboundStatisticService.getInboundSummary(pageable);
    }

    @Operation(
            parameters = {
                    @Parameter(name = "startMonth", example = "3" ,schema = @Schema(type = "integer")),
                    @Parameter(name = "endMonth", example = "5",schema = @Schema(type = "integer")),
                    @Parameter(name = "year", example = "2025",schema = @Schema(type = "integer"))
            },
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(type = "string", format = "binary")
                            ))
            })
    @GetMapping(value = "/inbound-summary-by-month",
            produces = "application/xlsx")
    public ResponseEntity<byte[]> getInboundSummaryByMonth(
            @RequestParam(name = "startMonth", defaultValue = "1") @Min(1) @Max(12) int startMonth,
            @RequestParam(name = "endMonth", defaultValue = "12") @Min(1) @Max(12) int endMonth,
            @RequestParam(name = "year") Integer year) {

        System.out.println("startMonth = " + startMonth);
        System.out.println("endMonth = " + endMonth);
        System.out.println("year = " + year);
        if (startMonth > endMonth) {
            throw new BadRequestException("Start month cannot be greater than end month");
        }
        if (year == null) {
            year = Year.now().getValue();
        }
        List<InboundSummaryMonthProjection> statisticData = inboundStatisticService.getInboundSummaryByMonth(startMonth, endMonth, year);

        System.out.println(statisticData);
        Map<Integer, List<InboundSummaryPerMonth>> groupdDataByMonth = InboundStatisticServiceImpl.extractAndGroupDataByMonth(statisticData);
        try (XSSFWorkbook workbook = ExcelServiceImpl.createSummaryWorkbook(groupdDataByMonth);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet("summary");

            int col1 = 4, col2 = 14;
            int idx = 1;
            int firstRow = -1;
            int lastRow = sheet.getLastRowNum();
            Integer currentMonth = null;
            int chartBaseRow = 1;

            while (idx <= lastRow) {
                Row row = sheet.getRow(idx);
                if (row == null) {
                    idx++;
                    continue;
                }

                Cell cell = row.getCell(0);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    int month = (int) cell.getNumericCellValue();

                    if (currentMonth != null && !Objects.equals(currentMonth, month)) {
                        int endRow = idx - 1;
                        int chartStartRow = chartBaseRow;
                        int chartEndRow = chartStartRow + 12;

                        ExcelServiceImpl.drawBarChart(
                                workbook,
                                firstRow, endRow,
                                col1, chartStartRow,
                                col2, chartEndRow,
                                currentMonth
                        );

                        chartBaseRow = chartEndRow + 2;
                        firstRow = idx;
                    } else if (currentMonth == null) {
                        firstRow = idx;
                    }
                    currentMonth = month;
                }
                idx++;
            }

            if (currentMonth != null && firstRow <= lastRow) {
                int endRow = lastRow;
                int chartStartRow = chartBaseRow;
                int chartEndRow = chartStartRow + 9;

                ExcelServiceImpl.drawBarChart(
                        workbook,
                        firstRow, endRow,
                        col1, chartStartRow,
                        col2, chartEndRow,
                        currentMonth
                );
            }

            workbook.write(out);
            byte[] fileContent = out.toByteArray();

            String fileName = URLEncoder.encode("InboundSummary-RPT2.xlsx", StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(fileContent);
        } catch (Exception e) {
            throw new RuntimeException("lỗi: "+e);
        }
    }
}
