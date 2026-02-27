package com.example.camunda.controller;

import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.StartProcessDto;
import com.example.camunda.service.CibProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/processes")
public class ProcessController {

    private final CibProcessService cibProcessService;

    public ProcessController(CibProcessService cibProcessService) {
        this.cibProcessService = cibProcessService;
    }

    @PostMapping("/{key}/start")
    public ResponseEntity<ProcessInstanceDto> startProcess(
            @PathVariable String key,
            @RequestBody(required = false) StartProcessDto startProcessDto) {
        if (startProcessDto == null) {
            startProcessDto = new StartProcessDto();
        }
        ProcessInstanceDto instance = cibProcessService.startProcess(key, startProcessDto);
        return ResponseEntity.ok(instance);
    }
}
