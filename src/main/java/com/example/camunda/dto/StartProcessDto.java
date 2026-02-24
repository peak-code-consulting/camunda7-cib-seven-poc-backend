package com.example.camunda.dto;

import lombok.Data;
import java.util.Map;

@Data
public class StartProcessDto {
    private String businessKey;
    private Map<String, Object> variables;
}
