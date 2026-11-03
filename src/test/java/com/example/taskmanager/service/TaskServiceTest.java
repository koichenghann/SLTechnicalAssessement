package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TaskService taskService;

    private User admin;
    private User ali;
    private User abu;

    @BeforeEach
    void setUp() {
        admin = user(1L, "admin", User.Role.ADMIN);
        abu = user(2L, "abu", User.Role.USER);
        ali = user(3L, "ali", User.Role.USER);
    }

    @Test
    void create_defaultsStatusToNew_andAssignsUser() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(ali));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(new TaskRequest("New task", null, null, 3L), "admin");

        assertThat(response.status()).isEqualTo(Task.Status.NEW);
        assertThat(response.assignedTo()).isEqualTo("ali");
        assertThat(response.createdBy()).isEqualTo("admin");
    }

    @Test
    void create_withUnknownAssignee_throwsNotFound() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(new TaskRequest("Task", null, null, 99L), "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void getById_userCanViewOwnTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(taskAssignedTo(ali)));
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));

        assertThat(taskService.getById(10L, "ali").id()).isEqualTo(10L);
    }

    @Test
    void getById_userCannotViewSomeoneElsesTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(taskAssignedTo(ali)));
        when(userRepository.findByUsername("abu")).thenReturn(Optional.of(abu));

        assertThatThrownBy(() -> taskService.getById(10L, "abu"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_missingTask_throwsNotFound() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(404L, "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Task not found with id: 404");
    }

    @Test
    void updateStatus_userCanUpdateOwnTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(taskAssignedTo(ali)));
        when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));
        when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(taskService.updateStatus(10L, Task.Status.COMPLETED, "ali").status())
                .isEqualTo(Task.Status.COMPLETED);
    }

    @Test
    void updateStatus_userCannotUpdateSomeoneElsesTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(taskAssignedTo(ali)));
        when(userRepository.findByUsername("abu")).thenReturn(Optional.of(abu));

        assertThatThrownBy(() -> taskService.updateStatus(10L, Task.Status.COMPLETED, "abu"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private User user(Long id, String username, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    private Task taskAssignedTo(User assignee) {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Task");
        task.setStatus(Task.Status.NEW);
        task.setAssignedUser(assignee);
        task.setCreatedBy(admin);
        return task;
    }
}
