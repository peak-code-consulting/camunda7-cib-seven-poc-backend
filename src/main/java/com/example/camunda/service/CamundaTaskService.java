package com.example.camunda.service;

import com.example.camunda.dto.CompleteTaskDto;
import com.example.camunda.dto.TaskDto;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class CamundaTaskService {

    private final ProcessEngine processEngine;

    public CamundaTaskService(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public List<TaskDto> getTasksForCurrentUser() {
        Set<String> userRoles = getCurrentUserRoles();
        String currentUserId = getCurrentUserId();

        List<Task> allTasks = processEngine.getTaskService().createTaskQuery().list();

        return allTasks.stream()
                .map(task -> {
                    TaskDto dto = toDto(task);
                    dto.setCanComplete(canUserCompleteTask(task, userRoles, currentUserId));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public TaskDto getTask(String taskId) {
        Task task = processEngine.getTaskService().createTaskQuery()
                .taskId(taskId)
                .singleResult();
        
        if (task == null) {
            return null;
        }

        Set<String> userRoles = getCurrentUserRoles();
        String currentUserId = getCurrentUserId();

        TaskDto dto = toDto(task);
        dto.setCanComplete(canUserCompleteTask(task, userRoles, currentUserId));
        return dto;
    }

    public void completeTask(String taskId, CompleteTaskDto completeTaskDto) {
        Set<String> userRoles = getCurrentUserRoles();
        String currentUserId = getCurrentUserId();

        Task task = processEngine.getTaskService().createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        if (!canUserCompleteTask(task, userRoles, currentUserId)) {
            throw new RuntimeException("User does not have permission to complete this task");
        }

        Map<String, Object> variables = completeTaskDto.getVariables();
        Map<String, Object> convertedVariables = convertVariables(variables);
        
        if (convertedVariables != null && !convertedVariables.isEmpty()) {
            processEngine.getTaskService().complete(taskId, convertedVariables);
        } else {
            processEngine.getTaskService().complete(taskId);
        }
    }

    public void claimTask(String taskId) {
        String currentUserId = getCurrentUserId();

        Task task = processEngine.getTaskService().createTaskQuery()
                .taskId(taskId)
                .singleResult();

        if (task == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }

        processEngine.getTaskService().setAssignee(taskId, currentUserId);
    }

    private Map<String, Object> convertVariables(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }
        Map<String, Object> converted = new HashMap<>();
        for (Entry<String, Object> entry : variables.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> varMap = (Map<String, Object>) value;
                if (varMap.containsKey("value")) {
                    converted.put(entry.getKey(), varMap.get("value"));
                } else {
                    converted.put(entry.getKey(), value);
                }
            } else {
                converted.put(entry.getKey(), value);
            }
        }
        return converted;
    }

    private boolean canUserCompleteTask(Task task, Set<String> userRoles, String currentUserId) {
        Set<String> candidateGroups = new HashSet<>();
        Set<String> candidateUsers = new HashSet<>();
        
        List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(task.getId());
        for (IdentityLink link : identityLinks) {
            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                candidateGroups.add(link.getGroupId());
            } else if ("candidate".equals(link.getType()) && link.getUserId() != null) {
                candidateUsers.add(link.getUserId());
            }
        }

        if (task.getAssignee() != null) {
            if (!task.getAssignee().equals(currentUserId)) {
                return false;
            }
            if (!candidateGroups.isEmpty()) {
                boolean hasRole = userRoles.stream().anyMatch(candidateGroups::contains);
                if (!hasRole) {
                    return false;
                }
            }
            return true;
        }

        if (!candidateGroups.isEmpty()) {
            boolean hasRole = userRoles.stream().anyMatch(candidateGroups::contains);
            if (!hasRole) {
                return false;
            }
        }

        if (!candidateUsers.isEmpty()) {
            if (!candidateUsers.contains(currentUserId)) {
                return false;
            }
        }

        return true;
    }

    private Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toSet());
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
        }
        return authentication.getName();
    }

    private TaskDto toDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setAssignee(task.getAssignee());
        dto.setOwner(task.getOwner());
        dto.setCreated(task.getCreateTime() != null ? task.getCreateTime().toString() : null);
        dto.setDue(task.getDueDate() != null ? task.getDueDate().toString() : null);
        dto.setPriority(task.getPriority());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setProcessDefinitionId(task.getProcessDefinitionId());
        dto.setTaskDefinitionKey(task.getTaskDefinitionKey());

        List<String> candidateGroups = new ArrayList<>();
        List<String> candidateUsers = new ArrayList<>();
        
        List<IdentityLink> identityLinks = processEngine.getTaskService().getIdentityLinksForTask(task.getId());
        for (IdentityLink link : identityLinks) {
            if ("candidate".equals(link.getType()) && link.getGroupId() != null) {
                candidateGroups.add(link.getGroupId());
            } else if ("candidate".equals(link.getType()) && link.getUserId() != null) {
                candidateUsers.add(link.getUserId());
            }
        }
        
        dto.setCandidateGroups(candidateGroups);
        dto.setCandidateUsers(candidateUsers);

        return dto;
    }
}
