package com.framework.websocket;

import com.framework.constants.ErrorMessages;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TodoWebSocketClient extends WebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(TodoWebSocketClient.class);
    private final List<String> messages = new ArrayList<>();
    private final CompletableFuture<Void> connected = new CompletableFuture<>();

    public TodoWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("WebSocket соединение открыто");
        connected.complete(null);
    }

    @Override
    public void onMessage(String message) {
        log.info("Получено сообщение: {}", message);
        messages.add(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket соединение закрыто: {} - {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        if (ex instanceof java.net.ConnectException) {
            log.error(ErrorMessages.WEBSOCKET_CONNECTION_ERROR, ex);
        } else {
            log.error(ErrorMessages.WEBSOCKET_MESSAGE_ERROR, ex);
        }
        connected.completeExceptionally(ex);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }

    public void waitForConnection(long timeout, TimeUnit unit) throws Exception {
        connected.get(timeout, unit);
    }
} 