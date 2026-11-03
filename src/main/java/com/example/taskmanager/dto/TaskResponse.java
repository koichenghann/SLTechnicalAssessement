package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Task;

import java.time.Instant;

public record TaskResponse(Long id, String title, String description, Task.Status status,
                           String assignedTo, String createdBy, Instant createdAt, Instant updatedAt) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : null,
                task.getCreatedBy().getUsername(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
