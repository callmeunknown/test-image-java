package com.framework.utils;

import com.framework.constants.ErrorMessages;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
    private static final Logger log = LoggerFactory.getLogger(LoggerUtil.class);

    public static void logRequest(String method, String endpoint, Object body) {
        log.info("----> {} запрос к {}", method, endpoint);
        if (body != null) {
            log.info("Тело запроса: {}", JsonUtil.toJson(body));
        }
    }

    public static void logResponse(Response response, String method, String endpoint) {
        int statusCode = response.getStatusCode();
        log.info("<---- {} {} - Получен ответ: {}", method, endpoint, statusCode);
        log.info("Тело ответа: {}", response.asPrettyString());
        
        if (statusCode >= 400) {
            String errorMessage = switch (statusCode) {
                case 400 -> ErrorMessages.BAD_REQUEST;
                case 401 -> ErrorMessages.UNAUTHORIZED;
                case 403 -> ErrorMessages.FORBIDDEN;
                case 404 -> ErrorMessages.NOT_FOUND;
                case 405 -> ErrorMessages.METHOD_NOT_ALLOWED;
                case 409 -> ErrorMessages.CONFLICT;
                case 500 -> ErrorMessages.INTERNAL_ERROR;
                case 503 -> ErrorMessages.SERVICE_UNAVAILABLE;
                default -> String.format(ErrorMessages.UNKNOWN_ERROR, statusCode);
            };
            log.error("Ошибка: {}", errorMessage);
        }
    }
} 