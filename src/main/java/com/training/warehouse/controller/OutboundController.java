package com.training.warehouse.controller;

import com.training.warehouse.common.util.FileUtil;
import com.training.warehouse.dto.response.StockProjection;
import com.training.warehouse.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.training.warehouse.dto.request.CreateOutboundRequest;
import com.training.warehouse.dto.response.CreateOutboundResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.OutboundService;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.OutboundService;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/outbound")
@io.swagger.v3.oas.annotations.tags.Tag(name = "outbound")
@AllArgsConstructor
@Validated
public class OutboundController {
    private final OutboundService outboundService;

    @io.swagger.v3.oas.annotations.Operation(
        method = "GET",
        summary = "confirm outbound by id",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(
                name = "bearerAuth"
            ),
        },
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "id",
                in = ParameterIn.PATH,
                required = true,
                schema = @Schema(
                    type = "integer",
                    format = "int64",
                    minimum = "1"
                )
            ),
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "inbound is confirmed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "invalid request data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "inbound not found",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "internal server error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
        }
    )
    @GetMapping("/{id}/confirm")
    public ResponseEntity<?> confirmById(
            @PathVariable @Min(value = 1, message = "outboundId must be greater than 0") long id) {
        byte[] mergedPdf = outboundService.confirmOutboundById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("outbound-" + id + "-confirmed.pdf")
                .build());
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(mergedPdf);
    }

    @Operation(
            parameters = {
                    @Parameter(name = "month", example = "3" ,schema = @Schema(type = "integer")),
                    @Parameter(name = "year", example = "2025",schema = @Schema(type = "integer"))
            },
            responses = {
                    @ApiResponse(responseCode = "200",
                            content = @Content(
                                    mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    schema = @Schema(type = "string", format = "binary")
                            ))
            })
    @GetMapping(value = "/stock-summary-by-month",produces = "application/xlsx")
    public ResponseEntity<byte[]> getStockSummaryByMonth(@RequestParam(name = "month", defaultValue = "1") @Min(1) @Max(12) int month,
                                                      @RequestParam(name = "year") Integer year) {
        if(year == null) year = Year.now().getValue();

        StockProjection data = outboundService.getStockSummaryByMonth(month,year);

        try (XSSFWorkbook workbook = FileUtil.createStockSummary(data);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int col1 = 4, col2 = 10;

            FileUtil.drawStockSummaryBarChart(workbook,col1,1,col2,10,month);

            workbook.write(out);
            byte[] fileContent = out.toByteArray();

            String fileName = URLEncoder.encode("StockSummary-RPT1.xlsx", StandardCharsets.UTF_8);

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

    @Operation(
            summary = "Import kế hoạch xuất kho từ file CSV",
            description = " Nhập danh sách kế hoạch xuất kho từ file CSV.",

            responses = {
                    @ApiResponse(responseCode = "200", description = "Import thành công hoặc có lỗi từng dòng",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Lỗi đọc file hoặc lỗi hệ thống")
            }
    )
    @PostMapping(value = "/import-export-plan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importOutbound(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = outboundService.importCsvExportPlan(file);
            if (result.containsKey("errorMessages")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        method = "GET",
        summary = "export late outbound orders grouped by month",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(
                name = "bearerAuth"
            ),
        },
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "start_month",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(type = "string", format = "date-time")
            ),
            @io.swagger.v3.oas.annotations.Parameter(
                name = "end_month",
                required = true,
                in = ParameterIn.QUERY,
                schema = @Schema(type = "string", format = "date-time")
            ),
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "success",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/octet-stream"
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "invalid request data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "internal server error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
        }
    )
    @GetMapping("/late-statistics")
    public ResponseEntity<?> getLateStatistics(
        @RequestParam("start_month") @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime startMonth,
        @RequestParam("end_month") @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime endMonth
    ) {
        byte[] fileBytes = outboundService.getLateOutboundStatistics(
            startMonth,
            endMonth
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
            ContentDisposition.attachment()
                .filename("late-outbound-statistics.xlsx")
                .build()
        );
        return ResponseEntity.status(HttpStatus.OK)
            .headers(headers)
            .body(fileBytes);
    }

    @io.swagger.v3.oas.annotations.Operation(
        method = "POST",
        summary = "create outbound",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(
                name = "bearerAuth"
            ),
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "request",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateOutboundRequest.class)
            )
        ),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "created",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = CreateOutboundResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "invalid request data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "internal server error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
        }
    )
    @PostMapping
    public ResponseEntity<CreateOutboundResponse> create(
        @RequestBody @Valid CreateOutboundRequest request
    ) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            this.outboundService.createOutbound(user, request)
        );
    }

    @io.swagger.v3.oas.annotations.Operation(
        method = "DELETE",
        summary = "delete outbound",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(
                name = "bearerAuth"
            ),
        },
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "id",
                in = ParameterIn.PATH,
                required = true,
                schema = @Schema(
                    type = "integer",
                    format = "int64",
                    minimum = "1"
                )
            ),
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "no content"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "invalid request data",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "unauthorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "not found",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "internal server error",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = ExceptionResponse.class
                    )
                )
            ),
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @Min(value = 1, message = "outboundId must be greater than 0") long id) {
        this.outboundService.deleteOutboundById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
