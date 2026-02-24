package com.example.camunda.service;

import com.example.camunda.dto.ProcessDefinitionDto;
import com.example.camunda.dto.ProcessInstanceDto;
import com.example.camunda.dto.StartProcessDto;
import com.example.camunda.dto.VariableDto;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;

    public ProcessService(ProcessEngine processEngine) {
        this.runtimeService = processEngine.getRuntimeService();
        this.repositoryService = processEngine.getRepositoryService();
    }

    public List<ProcessDefinitionDto> getProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProcessInstanceDto> getProcessInstancesByKey(String key) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(key)
                .orderByProcessInstanceId().desc()
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProcessInstanceDto startProcess(String key, StartProcessDto startProcessDto) {
        Map<String, Object> variables = startProcessDto.getVariables();
        if (variables == null) {
            variables = Map.of();
        }
        
        ProcessInstance instance;
        if (startProcessDto.getBusinessKey() != null && !startProcessDto.getBusinessKey().isEmpty()) {
            instance = runtimeService.startProcessInstanceByKey(key, startProcessDto.getBusinessKey(), variables);
        } else {
            instance = runtimeService.startProcessInstanceByKey(key, variables);
        }
        return toDto(instance);
    }

    public ProcessInstanceDto getProcessInstance(String id) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(id)
                .singleResult();
        if (instance == null) {
            return null;
        }
        return toDto(instance);
    }

    public List<VariableDto> getProcessVariables(String processInstanceId) {
        return runtimeService.createVariableInstanceQuery()
                .processInstanceIdIn(processInstanceId)
                .list()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public boolean deleteProcessInstance(String processInstanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (instance == null) {
            return false;
        }
        runtimeService.deleteProcessInstance(processInstanceId, "Deleted by user");
        return true;
    }

    private ProcessDefinitionDto toDto(ProcessDefinition definition) {
        ProcessDefinitionDto dto = new ProcessDefinitionDto();
        dto.setId(definition.getId());
        dto.setKey(definition.getKey());
        dto.setName(definition.getName());
        dto.setVersion(definition.getVersion());
        dto.setDeploymentId(definition.getDeploymentId());
        dto.setResource(definition.getResourceName());
        dto.setSuspensionState(definition.isSuspended() ? 1 : 2);
        return dto;
    }

    private ProcessInstanceDto toDto(ProcessInstance instance) {
        ProcessInstanceDto dto = new ProcessInstanceDto();
        dto.setId(instance.getId());
        dto.setProcessDefinitionId(instance.getProcessDefinitionId());
        
        // Fetch process definition info separately
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId())
                .singleResult();
        if (processDefinition != null) {
            dto.setProcessDefinitionKey(processDefinition.getKey());
            dto.setProcessDefinitionName(processDefinition.getName());
        }
        
        dto.setBusinessKey(instance.getBusinessKey());
        dto.setSuspended(instance.isSuspended());
        dto.setTenantId(instance.getTenantId());
        return dto;
    }

    private VariableDto toDto(VariableInstance variable) {
        VariableDto dto = new VariableDto();
        dto.setName(variable.getName());
        dto.setType(variable.getTypeName());
        dto.setValue(variable.getValue());
        return dto;
    }
}
