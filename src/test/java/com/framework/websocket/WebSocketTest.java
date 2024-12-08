package com.framework.websocket;

import com.framework.client.RestClient;
import com.framework.config.ConfigurationManager;
import com.framework.models.Todo;
import com.framework.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class WebSocketTest {
    private static final Logger log = LoggerFactory.getLogger(WebSocketTest.class);
    private final RestClient restClient = new RestClient();
    private final Random random = new Random();
    private TodoWebSocketClient wsClient;

    @BeforeEach
    void setUp() throws Exception {
        wsClient = new TodoWebSocketClient(new URI(ConfigurationManager.getInstance().getWebSocketUrl()));
        wsClient.connect();
        wsClient.waitForConnection(5, TimeUnit.SECONDS);
        wsClient.clearMessages();
    }

    @AfterEach
    void tearDown() {
        if (wsClient != null && !wsClient.isClosed()) {
            wsClient.close();
        }
    }

    private Todo createTodo(String text) {
        Todo todo = Todo.builder()
                .id(random.nextLong(10000))
                .text(text)
                .completed(false)
                .build();

        var response = restClient.post("/todos", todo).execute();
        assertThat(response.getStatusCode()).isEqualTo(201);
        return todo;
    }

    private Stream<JsonNode> getNotificationsForTodo(Todo todo) {
        return wsClient.getMessages().stream()
                .map(JsonUtil::parse)
                .filter(msg -> "new_todo".equals(msg.get("type").asText()))
                .filter(msg -> {
                    Todo msgTodo = JsonUtil.fromJson(msg.get("data").toString(), Todo.class);
                    return todo.getId().equals(msgTodo.getId());
                });
    }

    @Test
    void shouldReceiveNotificationForNewTodo() throws Exception {
        Todo todo = createTodo("Тестовая задача для WebSocket");
        Thread.sleep(1000);

        JsonNode notification = getNotificationsForTodo(todo).findFirst().orElse(null);
        assertThat(notification).isNotNull();
        
        Todo receivedTodo = JsonUtil.fromJson(notification.get("data").toString(), Todo.class);
        assertThat(receivedTodo)
                .usingRecursiveComparison()
                .isEqualTo(todo);
    }

    @Test
    void shouldReceiveNotificationsForMultipleTodos() throws Exception {
        Todo todo1 = createTodo("Первая задача");
        Todo todo2 = createTodo("Вторая задача");
        Todo todo3 = createTodo("Третья задача");
        Thread.sleep(1000);

        List<String> messages = wsClient.getMessages();
        assertThat(messages).hasSize(3);

        assertThat(getNotificationsForTodo(todo1).findFirst()).isPresent();
        assertThat(getNotificationsForTodo(todo2).findFirst()).isPresent();
        assertThat(getNotificationsForTodo(todo3).findFirst()).isPresent();
    }

    @Test
    void shouldHandleReconnection() throws Exception {
        Todo firstTodo = createTodo("Задача до переподключения");
        Thread.sleep(1000);
        assertThat(getNotificationsForTodo(firstTodo).findFirst()).isPresent();

        wsClient.close();
        Thread.sleep(1000);
        wsClient = new TodoWebSocketClient(new URI(ConfigurationManager.getInstance().getWebSocketUrl()));
        wsClient.connect();
        wsClient.waitForConnection(5, TimeUnit.SECONDS);
        wsClient.clearMessages();

        Todo secondTodo = createTodo("Задача после переподключения");
        Thread.sleep(1000);
        assertThat(getNotificationsForTodo(secondTodo).findFirst()).isPresent();
    }
}