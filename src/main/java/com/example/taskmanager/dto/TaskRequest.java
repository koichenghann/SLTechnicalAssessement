package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Task;

/** Body for creating and updating a task. Field rules are on the Task entity. */
public record TaskRequest(String title, String description, Task.Status status, Long assignedUserId) {
}
