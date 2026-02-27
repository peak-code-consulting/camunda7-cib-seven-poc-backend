package com.example.camunda.controller;

import com.example.camunda.dto.ProcessDefinitionDto;
import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.StartProcessDto;
import com.example.camunda.service.CibProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-definitions")
public class ProcessDefinitionController {

    private final CibProcessService cibProcessService;

    public ProcessDefinitionController(CibProcessService cibProcessService) {
        this.cibProcessService = cibProcessService;
    }

    @GetMapping
    public List<ProcessDefinitionDto> getProcessDefinitions() {
        return cibProcessService.getProcessDefinitions();
    }

    @GetMapping("/{key}/instances")
    public List<ProcessInstanceDto> getProcessInstances(@PathVariable String key) {
        return cibProcessService.getProcessInstancesByKey(key);
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
