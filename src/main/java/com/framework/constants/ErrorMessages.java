package com.framework.constants;

public class ErrorMessages {
    // HTTP Status Code Errors
    public static final String NOT_FOUND = "Ресурс не найден";
    public static final String BAD_REQUEST = "Неверный запрос";
    public static final String INTERNAL_ERROR = "Внутренняя ошибка сервера";
    public static final String UNAUTHORIZED = "Не авторизован";
    public static final String FORBIDDEN = "Доступ запрещен";
    public static final String METHOD_NOT_ALLOWED = "Метод не разрешен";
    public static final String CONFLICT = "Конфликт данных";
    public static final String SERVICE_UNAVAILABLE = "Сервис недоступен";

    public static final String WEBSOCKET_CONNECTION_ERROR = "Ошибка подключения к WebSocket";
    public static final String WEBSOCKET_MESSAGE_ERROR = "Ошибка при отправке/получении сообщения WebSocket";
    
    // Generic Errors
    public static final String UNKNOWN_ERROR = "Неизвестная ошибка: %s";
} 