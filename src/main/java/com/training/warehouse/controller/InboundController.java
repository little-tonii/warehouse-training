package com.training.warehouse.controller;

import com.training.warehouse.common.util.FileUtil;
import com.training.warehouse.common.util.InboundUtil;
import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.service.MailService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.constraints.Max;
import lombok.Value;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.InboundService;
import com.training.warehouse.service.InboundStatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Min;


@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InboundController {
    private final InboundService inboundService;
    private final InboundStatisticService inboundStatisticService;
    private final MailService mailService;
    @Operation(
            method = "DELETE",
            summary = "delete inbound by id",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "integer", format = "int64", minimum = "1")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "inbound deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "invalid request data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "inbound not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "internal server error",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
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
            method = "POST",
            summary = "create inbound",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = InboundCreateRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "create inbound successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = InboundResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "invalid request data",
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
                            description = "Forbidden – Access denied",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found – Resource does not exist",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict – Duplicate or invalid state",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal Server Error – Unexpected error occurred",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceptionResponse.class)
                            )
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createInbound(@ModelAttribute @Valid InboundCreateRequest request) {
        return ResponseEntity.ok(inboundService.createInbound(request));
    }

    @Operation(
            method = "PUT",
            summary = "update inbound",
            parameters = {
                    @Parameter(
                            name = "id",
                            in = ParameterIn.PATH,
                            required = true,
                            example = "123"
                    )
            },
            requestBody = @RequestBody(
                    required = true,
                    content = {
                            @Content(
                                    mediaType = "multipart/form-data",
                                    schema = @Schema(implementation = InboundUpdateRequest.class)
                            )
                    }
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inbound updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = InboundResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request – Invalid input",
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
                            description = "Forbidden – No permission to update",
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
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateInbound(@PathVariable Long id,
                                           @ModelAttribute @Valid InboundUpdateRequest request) {

        return ResponseEntity.ok(inboundService.updateInbound(id, request));
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
            produces = "*/*")
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
        Map<Integer, List<InboundSummaryPerMonth>> groupdDataByMonth = InboundUtil.extractAndGroupDataByMonth(statisticData);
        try (XSSFWorkbook workbook = FileUtil.createSummaryWorkbook(groupdDataByMonth);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.getSheet("summary");

            int idx = 1;
            int col1 = 4, col2 = 12, row1, row2;
            int firstRow;
            int endRow;
            int month;
            while (idx <= sheet.getLastRowNum()) {
                Row row = sheet.getRow(idx);
                if (row == null) {
                    idx++;
                    continue;
                }
                XSSFCell cell = (XSSFCell) row.getCell(0);
                firstRow = idx;
                row1 = idx;
                month = Integer.parseInt(cell.getRawValue());
                if (cell == null || cell.getCellType() == CellType.BLANK || cell.toString().trim().isEmpty()) {
                    idx++;
                    continue;
                }
                endRow = idx - 1;
                row2 = idx - 1;
                FileUtil.drawBarChart(workbook, firstRow, endRow, col1, row1, col2, row2, month);
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
            throw new RuntimeException(e);
        }
    }

    @Operation(
            summary = "Import Inbound Data from CSV file",
            method = "POST",

            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Import Result File Response",
                            content =
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object")
                            )

                    )
            }
    )
    @PostMapping(value = "/import-inbound-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importInboundDataFile(@Parameter(description = "CSV file", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
                                                   @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = inboundService.importFromCsv(file);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "send alert",
            method= "GET",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object")
                            )
                    )
            }
    )
    @GetMapping("/send")
    public ResponseEntity<?> sendMail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content
    ) {
        mailService.sendMail(to, subject, content);
        return ResponseEntity.ok("Đã gửi mail test tới " + to);
    }
}