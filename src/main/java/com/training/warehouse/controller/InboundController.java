package com.training.warehouse.controller;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import com.training.warehouse.dto.response.InboundSummaryResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InboundController {

    private final InboundService inboundService;
    private final InboundStatisticService inboundStatisticService;

    @io.swagger.v3.oas.annotations.Operation(
            method = "POST",
            summary = "create inbound",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
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
    @PostMapping
    public ResponseEntity<?> createInbound(@Valid @RequestBody InboundCreateRequest request) {
        return ResponseEntity.ok(inboundService.createInbound(request));
    }

    @Operation(
            method = "PUT",
            summary = "update inbound",
            parameters = {
                    @Parameter(
                            name = "id",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "123"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InboundCreateRequest.class)
                    )
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
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInbound(@PathVariable Long id, @Valid @RequestBody InboundUpdateRequest request) {
        return ResponseEntity.ok(inboundService.updateInbound(id, request));
    }

    @Operation(
            method = "DELETE",
            summary = "Delete inbound by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Inbound deleted successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "\"Deleted successfully\"")
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
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInbound(@PathVariable Long id) {
        inboundService.deleteInbound(id);
        return ResponseEntity.ok("Deleted successfully");
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

    @PostMapping("/import-inbound-data")
    public ResponseEntity<?> importInboundDataFile(){
        return ResponseEntity.ok("");
    }
}
