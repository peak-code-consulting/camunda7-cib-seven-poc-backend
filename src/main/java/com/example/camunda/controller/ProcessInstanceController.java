package com.example.camunda.controller;

import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.VariableDto;
import com.example.camunda.service.CibProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process-instances")
public class ProcessInstanceController {

    private final CibProcessService cibProcessService;

    public ProcessInstanceController(CibProcessService cibProcessService) {
        this.cibProcessService = cibProcessService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessInstanceDto> getProcessInstance(@PathVariable String id) {
        ProcessInstanceDto instance = cibProcessService.getProcessInstance(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instance);
    }

    @GetMapping("/{id}/variables")
    public ResponseEntity<List<VariableDto>> getProcessVariables(@PathVariable String id) {
        ProcessInstanceDto instance = cibProcessService.getProcessInstance(id);
        if (instance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cibProcessService.getProcessVariables(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessInstance(@PathVariable String id) {
        boolean deleted = cibProcessService.deleteProcessInstance(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
