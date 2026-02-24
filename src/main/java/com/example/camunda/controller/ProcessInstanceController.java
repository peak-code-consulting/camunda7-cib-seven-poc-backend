package com.example.camunda.controller;

import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.VariableDto;
import com.example.camunda.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {

    private final ProcessService processService;

    public ProcessInstanceController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessInstanceDto> getProcessInstance(@PathVariable String id) {
        ProcessInstanceDto instance = processService.getProcessInstance(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instance);
    }

    @GetMapping("/{id}/variables")
    public ResponseEntity<List<VariableDto>> getProcessVariables(@PathVariable String id) {
        ProcessInstanceDto instance = processService.getProcessInstance(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(processService.getProcessVariables(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessInstance(@PathVariable String id) {
        boolean deleted = processService.deleteProcessInstance(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
