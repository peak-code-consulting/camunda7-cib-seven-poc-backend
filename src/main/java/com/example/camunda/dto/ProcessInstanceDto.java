package com.example.camunda.dto;

import lombok.Data;

@Data
public class ProcessInstanceDto {
    private String id;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String businessKey;
    private String activityId;
    private Boolean suspended;
    private String tenantId;
}
