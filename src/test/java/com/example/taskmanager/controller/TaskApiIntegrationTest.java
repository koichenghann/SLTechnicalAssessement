package com.example.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests through the HTTP layer, security and an H2 database loaded with database/*.sql.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_withMissingFields_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void request_withoutLogin_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in first"));
    }

    @Test
    void admin_canCreateAssignUpdateAndDeleteTask() throws Exception {
        MockHttpSession admin = login("admin", "admin123");

        String body = mockMvc.perform(post("/api/tasks").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Integration task\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdBy").value("admin"))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(put("/api/tasks/{id}/assign/{userId}", taskId, 3).session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("ali"));

        mockMvc.perform(put("/api/tasks/{id}", taskId).session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"description\":\"new\",\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(delete("/api/tasks/{id}", taskId).session(admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId).session(admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));
    }

    @Test
    void user_seesOnlyAssignedTasks_andCannotUseAdminEndpoints() throws Exception {
        MockHttpSession ali = login("ali", "user123");

        mockMvc.perform(get("/api/tasks").session(ali))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].assignedTo", everyItem(is("ali"))));

        mockMvc.perform(post("/api/tasks").session(ali)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Not allowed\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/tasks/1").session(ali))
                .andExpect(status().isForbidden());

        // Task 3 is assigned to abu in the seed data
        mockMvc.perform(get("/api/tasks/3").session(ali))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only view tasks assigned to you"));
    }

    @Test
    void user_canUpdateStatusOfOwnTask() throws Exception {
        MockHttpSession ali = login("ali", "user123");

        // Task 2 is assigned to ali in the seed data
        mockMvc.perform(patch("/api/tasks/2/status").session(ali)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void createTask_withInvalidBody_returns400() throws Exception {
        MockHttpSession admin = login("admin", "admin123");

        mockMvc.perform(post("/api/tasks").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required"));

        mockMvc.perform(post("/api/tasks").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/tasks/1").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required"));

        mockMvc.perform(patch("/api/tasks/1/status").session(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").value("Status is required"));
    }

    @Test
    void logout_endsTheSession() throws Exception {
        MockHttpSession abu = login("abu", "user123");

        mockMvc.perform(post("/api/auth/logout").session(abu))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks").session(abu))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
