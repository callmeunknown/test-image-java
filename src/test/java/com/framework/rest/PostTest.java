package com.framework.rest;

import com.framework.client.RestClient;
import com.framework.models.Todo;
import com.framework.utils.LoggerUtil;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class PostTest {
    private static final Logger log = LoggerFactory.getLogger(PostTest.class);
    private final RestClient restClient = new RestClient();
    private final Random random = new Random();

    @Test
    void shouldCreateNewTodoAndReturn201() {
        // Arrange
        long randomId = random.nextInt(10000);
        Todo newTodo = Todo.builder()
                .id(randomId)
                .text("Тестовая задача " + randomId)
                .completed(false)
                .build();

        // Act
        Response response = restClient.post("/todos", newTodo).execute();
        LoggerUtil.logResponse(response, "POST", "/todos");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(201);
    }

    @Test
    void shouldReturn400WhenCreatingTodoWithDuplicateId() {
        // Arrange
        long todoId = random.nextInt(10000);
        Todo todo = Todo.builder()
                .id(todoId)
                .text("Задача с дубликатом id")
                .completed(false)
                .build();

        // Act - создаем первую задачу
        Response firstResponse = restClient.post("/todos", todo).execute();
        assertThat(firstResponse.getStatusCode()).isEqualTo(201);

        // Act - пытаемся создать задачу с тем же id
        Response response = restClient.post("/todos", todo).execute();
        LoggerUtil.logResponse(response, "POST", "/todos");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenIdIsMissing() {
        // Arrange
        Todo invalidTodo = Todo.builder()
                .text("Задача без id")
                .completed(false)
                .build();

        // Act
        Response response = restClient.post("/todos", invalidTodo).execute();
        LoggerUtil.logResponse(response, "POST", "/todos");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().asString())
                .contains("invalid type: null, expected u64");
    }

    @Test
    void shouldReturn400WhenTextIsMissing() {
        // Arrange
        Todo invalidTodo = Todo.builder()
                .id(random.nextLong(10000))
                .completed(false)
                .build();

        // Act
        Response response = restClient.post("/todos", invalidTodo).execute();
        LoggerUtil.logResponse(response, "POST", "/todos");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().asString())
                .contains("invalid type: null, expected a string");
    }

    @Test
    void shouldReturn400WhenFieldTypesAreInvalid() {
        // Arrange - создаем невалидный JSON напрямую, так как Todo не позволит создать объект с неверными типами
        String invalidTodoJson = """
                {
                    "id": "не число",
                    "text": 12345,
                    "completed": "не булево значение"
                }
                """;

        // Act
        Response response = restClient.post("/todos", invalidTodoJson).execute();
        LoggerUtil.logResponse(response, "POST", "/todos");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody().asString())
                .contains("invalid type: string \"не число\", expected u64");
    }
} 