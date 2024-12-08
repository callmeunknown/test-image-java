package com.framework.rest;

import com.framework.client.RestClient;
import com.framework.config.AuthConfig;
import com.framework.models.Todo;
import com.framework.utils.LoggerUtil;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteTest {
    private static final Logger log = LoggerFactory.getLogger(DeleteTest.class);
    private static final String INVALID_AUTH = "Basic aW52YWxpZDppbnZhbGlk";
    
    private final RestClient restClient = new RestClient();
    private final Random random = new Random();
    private Long createdTodoId;

    @BeforeEach
    void setUp() {
        long randomId = random.nextLong(10000);
        Todo newTodo = Todo.builder()
                .id(randomId)
                .text("Test TODO " + randomId)
                .completed(false)
                .build();

        Response response = restClient.post("/todos", newTodo).execute();
        assertThat(response.getStatusCode()).isEqualTo(201);
        createdTodoId = randomId;
        log.info("Создана тестовая задача с id: {}", createdTodoId);
    }

    @Test
    void shouldSuccessfullyDeleteExistingTodo() {
        Response response = restClient.delete("/todos/" + createdTodoId)
                .header("Authorization", AuthConfig.ADMIN_AUTH)
                .execute();
        LoggerUtil.logResponse(response, "DELETE", "/todos/" + createdTodoId);

        assertThat(response.getStatusCode()).isEqualTo(204);

        Response checkResponse = restClient.get("/todos").execute();
        List<Todo> todos = checkResponse.jsonPath().getList("", Todo.class);
        assertThat(todos).noneMatch(todo -> todo.getId().equals(createdTodoId));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTodo() {
        long nonExistentId = 99999L;

        Response response = restClient.delete("/todos/" + nonExistentId)
                .header("Authorization", AuthConfig.ADMIN_AUTH)
                .execute();
        LoggerUtil.logResponse(response, "DELETE", "/todos/" + nonExistentId);

        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldReturn401WhenAuthorizationHeaderIsMissing() {
        Response response = restClient.delete("/todos/" + createdTodoId)
                .execute();
        LoggerUtil.logResponse(response, "DELETE", "/todos/" + createdTodoId);

        assertThat(response.getStatusCode()).isEqualTo(401);
    }

    @Test
    void shouldReturn404WhenDeletingTodoTwice() {
        Response firstResponse = restClient.delete("/todos/" + createdTodoId)
                .header("Authorization", AuthConfig.ADMIN_AUTH)
                .execute();
        assertThat(firstResponse.getStatusCode()).isEqualTo(204);

        Response secondResponse = restClient.delete("/todos/" + createdTodoId)
                .header("Authorization", AuthConfig.ADMIN_AUTH)
                .execute();
        LoggerUtil.logResponse(secondResponse, "DELETE", "/todos/" + createdTodoId);

        assertThat(secondResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldReturn404WhenIdFormatIsInvalid() {
        Response response = restClient.delete("/todos/invalid-id")
                .header("Authorization", AuthConfig.ADMIN_AUTH)
                .execute();
        LoggerUtil.logResponse(response, "DELETE", "/todos/invalid-id");

        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() {
        Response response = restClient.delete("/todos/" + createdTodoId)
                .header("Authorization", INVALID_AUTH)
                .execute();
        LoggerUtil.logResponse(response, "DELETE", "/todos/" + createdTodoId);

        assertThat(response.getStatusCode()).isEqualTo(401);
    }
} 