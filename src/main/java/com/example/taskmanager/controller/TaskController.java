package com.example.taskmanager.controller;

import com.example.taskmanager.dto.StatusRequest;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> create(@RequestBody TaskRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, auth.getName()));
    }

    /** ADMIN gets all tasks, USER gets only the tasks assigned to them. */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(Authentication auth) {
        return ResponseEntity.ok(taskService.getAll(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(taskService.getById(id, auth.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    /** ADMIN can change any task's status, USER only the status of their own tasks. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(@PathVariable Long id, @RequestBody StatusRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(taskService.updateStatus(id, request.status(), auth.getName()));
    }

    @PutMapping("/{id}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> assign(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.assign(id, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
