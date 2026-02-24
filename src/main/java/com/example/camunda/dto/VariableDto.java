package com.example.camunda.dto;

import lombok.Data;

@Data
public class VariableDto {
    private String name;
    private String type;
    private Object value;
}
