package com.training.warehouse.controller;

import com.training.warehouse.dto.request.InboundRequest;
import com.training.warehouse.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    @PostMapping
    public ResponseEntity<?> createInbound(@RequestBody InboundRequest request) {
        return ResponseEntity.ok(inboundService.createInbound(request));
    }
}
