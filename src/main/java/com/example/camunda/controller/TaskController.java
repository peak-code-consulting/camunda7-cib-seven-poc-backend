package com.example.camunda.controller;

import com.example.camunda.dto.CompleteTaskDto;
import com.example.camunda.dto.TaskDto;
import com.example.camunda.service.CamundaTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final CamundaTaskService camundaTaskService;

    public TaskController(CamundaTaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    @GetMapping
    public List<TaskDto> getTasks() {
        return camundaTaskService.getTasksForCurrentUser();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable String id) {
        TaskDto task = camundaTaskService.getTask(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String id,
            @RequestBody(required = false) CompleteTaskDto completeTaskDto) {
        if (completeTaskDto == null) {
            completeTaskDto = new CompleteTaskDto();
        }
        try {
            camundaTaskService.completeTask(id, completeTaskDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Void> claimTask(@PathVariable String id) {
        try {
            camundaTaskService.claimTask(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
