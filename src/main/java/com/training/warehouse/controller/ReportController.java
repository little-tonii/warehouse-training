package com.training.warehouse.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.training.warehouse.dto.request.ReportMonthlyRequest;
import com.training.warehouse.dto.response.ReportMonthlyResponse;
import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.ReportService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/api/report")
@AllArgsConstructor
public class ReportController {
    
    private final ReportService reportService;


    @io.swagger.v3.oas.annotations.Operation(
        method = "GET",
        summary = "get monthly report",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
        },
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "monthly report retrieved successfully",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ReportMonthlyResponse.class)
                )
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
                description = "unauthorized access",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExceptionResponse.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "forbidden access",
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
    @GetMapping("/monthly")
    public ResponseEntity<ReportMonthlyResponse> getReportMonthly(@Valid @ParameterObject ReportMonthlyRequest request) {
        ReportMonthlyResponse response = reportService.getReportMonthly(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
