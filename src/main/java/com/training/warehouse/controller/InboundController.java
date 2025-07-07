package com.training.warehouse.controller;

import com.training.warehouse.dto.request.InboundRequest;
import com.training.warehouse.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;
    @PostMapping
    public ResponseEntity<?> createInbound(@RequestBody InboundRequest request) {
        return ResponseEntity.ok(inboundService.createInbound(request));
    }

    @PutMapping("/${id}")
    public ResponseEntity<?> updateInbound(@PathVariable Long id,@RequestBody InboundRequest request){
        return ResponseEntity.ok(inboundService.updateInbound(id,request));
    }

    @DeleteMapping("${id}")
    public ResponseEntity deleteInbound(@PathVariable Long id){
        return ResponseEntity.ok().build();
    }
}
