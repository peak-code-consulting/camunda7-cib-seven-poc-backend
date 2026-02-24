package com.example.camunda.dto;

import lombok.Data;

@Data
public class ProcessDefinitionDto {
    private String id;
    private String key;
    private String name;
    private Integer version;
    private String deploymentId;
    private String resource;
    private Integer suspensionState;
}
