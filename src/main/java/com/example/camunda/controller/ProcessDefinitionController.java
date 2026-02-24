package com.example.camunda.controller;

import com.example.camunda.dto.ProcessDefinitionDto;
import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.StartProcessDto;
import com.example.camunda.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-definitions")
public class ProcessDefinitionController {

    private final ProcessService processService;

    public ProcessDefinitionController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping
    public List<ProcessDefinitionDto> getProcessDefinitions() {
        return processService.getProcessDefinitions();
    }

    @GetMapping("/{key}/instances")
    public List<ProcessInstanceDto> getProcessInstances(@PathVariable String key) {
        return processService.getProcessInstancesByKey(key);
    }

    @PostMapping("/{key}/start")
    public ResponseEntity<ProcessInstanceDto> startProcess(
            @PathVariable String key,
            @RequestBody(required = false) StartProcessDto startProcessDto) {
        if (startProcessDto == null) {
            startProcessDto = new StartProcessDto();
        }
        ProcessInstanceDto instance = processService.startProcess(key, startProcessDto);
        return ResponseEntity.ok(instance);
    }
}
