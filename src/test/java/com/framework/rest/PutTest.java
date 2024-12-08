package com.framework.rest;

import com.framework.client.RestClient;
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

public class PutTest {
    private static final Logger log = LoggerFactory.getLogger(PutTest.class);
    private final RestClient restClient = new RestClient();
    private final Random random = new Random();
    private Long createdTodoId;

    @BeforeEach
    void setUp() {
        // Создаем тестовую задачу
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
    void shouldReturn404WhenUpdatingNonExistentTodo() {
        // Arrange
        long nonExistentId = 99999L;
        Todo updatedTodo = Todo.builder()
                .id(nonExistentId)
                .text("Обновление несуществующей задачи")
                .completed(true)
                .build();

        // Act
        Response response = restClient.put("/todos/" + nonExistentId, updatedTodo).execute();
        LoggerUtil.logResponse(response, "PUT", "/todos/" + nonExistentId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldReturn401WhenUpdatingWithoutRequiredTextField() {
        // Arrange
        Todo invalidUpdate = Todo.builder()
                .id(createdTodoId)
                .completed(true)
                .build();

        // Act
        Response response = restClient.put("/todos/" + createdTodoId, invalidUpdate).execute();
        LoggerUtil.logResponse(response, "PUT", "/todos/" + createdTodoId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(401);
    }

    @Test
    void shouldReturn401WhenFieldTypesAreInvalid() {
        // Arrange
        String invalidTodoJson = """
                {
                    "id": %d,
                    "text": 12345,
                    "completed": "не булево значение"
                }
                """.formatted(createdTodoId);

        // Act
        Response response = restClient.put("/todos/" + createdTodoId, invalidTodoJson).execute();
        LoggerUtil.logResponse(response, "PUT", "/todos/" + createdTodoId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(401);
    }

    @Test
    void shouldUpdateTodoWithAllFields() {
        // Arrange
        Todo fullUpdate = Todo.builder()
                .id(createdTodoId)
                .text("Полностью обновленная задача")
                .completed(true)
                .build();

        // Act
        Response response = restClient.put("/todos/" + createdTodoId, fullUpdate).execute();
        LoggerUtil.logResponse(response, "PUT", "/todos/" + createdTodoId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Проверяем, что задача действительно обновлена
        Response checkResponse = restClient.get("/todos").execute();
        List<Todo> todos = checkResponse.jsonPath().getList("", Todo.class);
        Todo updatedTodo = todos.stream()
                .filter(todo -> todo.getId().equals(createdTodoId))
                .findFirst()
                .orElse(null);

        assertThat(updatedTodo)
                .isNotNull()
                .satisfies(todo -> {
                    assertThat(todo.getText()).isEqualTo(fullUpdate.getText());
                    assertThat(todo.isCompleted()).isEqualTo(fullUpdate.isCompleted());
                });
    }

    @Test
    void shouldUpdateOnlyCompletedField() {
        // Arrange - получаем текущее состояние задачи
        Response getResponse = restClient.get("/todos").execute();
        List<Todo> todos = getResponse.jsonPath().getList("", Todo.class);
        Todo currentTodo = todos.stream()
                .filter(todo -> todo.getId().equals(createdTodoId))
                .findFirst()
                .orElseThrow();

        Todo partialUpdate = Todo.builder()
                .id(createdTodoId)
                .text(currentTodo.getText())
                .completed(!currentTodo.isCompleted())
                .build();

        // Act
        Response response = restClient.put("/todos/" + createdTodoId, partialUpdate).execute();
        LoggerUtil.logResponse(response, "PUT", "/todos/" + createdTodoId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Проверяем, что обновилось только поле completed
        Response checkResponse = restClient.get("/todos").execute();
        List<Todo> updatedTodos = checkResponse.jsonPath().getList("", Todo.class);
        Todo updatedTodo = updatedTodos.stream()
                .filter(todo -> todo.getId().equals(createdTodoId))
                .findFirst()
                .orElseThrow();

        assertThat(updatedTodo)
                .isNotNull()
                .satisfies(todo -> {
                    assertThat(todo.getText()).isEqualTo(currentTodo.getText());
                    assertThat(todo.isCompleted()).isEqualTo(!currentTodo.isCompleted());
                });
    }
} 