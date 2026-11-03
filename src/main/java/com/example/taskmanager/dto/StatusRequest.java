package com.example.taskmanager.dto;

import com.example.taskmanager.entity.Task;

public record StatusRequest(Task.Status status) {
}
