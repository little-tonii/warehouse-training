package com.training.warehouse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.training.warehouse.exception.handler.ExceptionResponse;
import com.training.warehouse.service.InboundService;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.response.CreateOutboundResponse;
import com.training.warehouse.entity.UserEntity;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/inbound")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "inbound")
@Validated
public class InboundController {
    private final InboundService inboundService;


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

    
    @io.swagger.v3.oas.annotations.Operation(
        method = "POST",
        summary = "create inbound",
        security = {
            @io.swagger.v3.oas.annotations.security.SecurityRequirement(
                name = "bearerAuth"
            ),
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "request", 
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "multipart/form-data",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = CreateInboundRequest.class)
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createInbound(@ModelAttribute @Valid CreateInboundRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(inboundService.createInbound(user, request));
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
}
