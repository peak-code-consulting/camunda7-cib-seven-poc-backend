package com.example.camunda.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskDto {
    private String id;
    private String name;
    private String assignee;
    private String owner;
    private String created;
    private String due;
    private Integer priority;
    private String processInstanceId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private List<String> candidateGroups;
    private List<String> candidateUsers;
    private Boolean canComplete;
}
