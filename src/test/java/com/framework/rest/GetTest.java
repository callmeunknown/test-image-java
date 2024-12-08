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

public class GetTest {
    private static final Logger log = LoggerFactory.getLogger(GetTest.class);
    private final RestClient restClient = new RestClient();
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        for (int i = 0; i < 3; i++) {
            long randomId = random.nextInt(10000);
            Todo todo = Todo.builder()
                    .id(randomId)
                    .text("Test TODO " + randomId)
                    .completed(i % 2 == 0)
                    .build();

            Response response = restClient.post("/todos", todo).execute();
            assertThat(response.getStatusCode()).isEqualTo(201);
            log.info("Создана тестовая задача: {}", todo);
        }
    }

    @Test
    void shouldReturnAllTodosWithoutParameters() {
        Response response = restClient.get("/todos").execute();
        LoggerUtil.logResponse(response, "GET", "/todos");

        assertThat(response.getStatusCode()).isEqualTo(200);
        List<Todo> todos = response.jsonPath().getList("", Todo.class);
        
        assertThat(todos)
                .isNotNull()
                .hasSizeGreaterThanOrEqualTo(3);

        Todo firstTodo = todos.get(0);
        assertThat(firstTodo)
                .hasFieldOrProperty("id")
                .hasFieldOrProperty("text")
                .hasFieldOrProperty("completed");
    }

    @Test
    void shouldReturnLimitedTodos() {
        int limit = 2;
        String endpoint = "/todos?limit=" + limit;
        Response response = restClient.get(endpoint).execute();
        LoggerUtil.logResponse(response, "GET", endpoint);

        assertThat(response.getStatusCode()).isEqualTo(200);
        List<Todo> todos = response.jsonPath().getList("", Todo.class);
        assertThat(todos).hasSizeLessThanOrEqualTo(limit);
    }

    @Test
    void shouldSkipTodosWithOffset() {
        int offset = 1;
        String endpoint = "/todos?offset=" + offset;
        Response response = restClient.get(endpoint).execute();
        LoggerUtil.logResponse(response, "GET", endpoint);

        assertThat(response.getStatusCode()).isEqualTo(200);
        List<Todo> todos = response.jsonPath().getList("", Todo.class);
        assertThat(todos).isNotNull();
    }

    @Test
    void shouldApplyBothOffsetAndLimit() {
        int offset = 1;
        int limit = 1;
        String endpoint = "/todos?offset=" + offset + "&limit=" + limit;
        Response response = restClient.get(endpoint).execute();
        LoggerUtil.logResponse(response, "GET", endpoint);

        assertThat(response.getStatusCode()).isEqualTo(200);
        List<Todo> todos = response.jsonPath().getList("", Todo.class);
        assertThat(todos).hasSizeLessThanOrEqualTo(limit);
    }

    @Test
    void shouldReturn400ForInvalidParameters() {
        String endpoint = "/todos?offset=invalid&limit=abc";
        Response response = restClient.get(endpoint).execute();
        LoggerUtil.logResponse(response, "GET", endpoint);

        assertThat(response.getStatusCode()).isEqualTo(400);
    }
} 