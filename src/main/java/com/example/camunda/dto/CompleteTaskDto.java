package com.example.camunda.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CompleteTaskDto {
    private Map<String, Object> variables;
}
