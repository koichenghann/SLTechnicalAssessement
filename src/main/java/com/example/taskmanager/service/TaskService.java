package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Task business rules. Admin-only operations are protected in TaskController with @PreAuthorize;
 * the "USER can only see / update their own tasks" rule is checked here.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse create(TaskRequest request, String username) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : Task.Status.NEW);
        task.setCreatedBy(findUserByUsername(username));
        if (request.assignedUserId() != null) {
            task.setAssignedUser(findUser(request.assignedUserId()));
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll(String username) {
        User user = findUserByUsername(username);
        List<Task> tasks = isAdmin(user)
                ? taskRepository.findAllByOrderByIdAsc()
                : taskRepository.findByAssignedUserOrderByIdAsc(user);
        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id, String username) {
        Task task = findTask(id);
        User user = findUserByUsername(username);
        if (!isAdmin(user) && !isAssignedTo(task, user)) {
            throw new AccessDeniedException("You can only view tasks assigned to you");
        }
        return TaskResponse.from(task);
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTask(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.assignedUserId() != null) {
            task.setAssignedUser(findUser(request.assignedUserId()));
        }
        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    public TaskResponse updateStatus(Long id, Task.Status status, String username) {
        Task task = findTask(id);
        User user = findUserByUsername(username);
        if (!isAdmin(user) && !isAssignedTo(task, user)) {
            throw new AccessDeniedException("You can only update the status of tasks assigned to you");
        }
        task.setStatus(status);
        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    public TaskResponse assign(Long id, Long userId) {
        Task task = findTask(id);
        task.setAssignedUser(findUser(userId));
        return TaskResponse.from(taskRepository.saveAndFlush(task));
    }

    public void delete(Long id) {
        taskRepository.delete(findTask(id));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }

    private boolean isAdmin(User user) {
        return user.getRole() == User.Role.ADMIN;
    }

    private boolean isAssignedTo(Task task, User user) {
        return task.getAssignedUser() != null && task.getAssignedUser().getId().equals(user.getId());
    }
}
