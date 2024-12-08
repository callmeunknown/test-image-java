package com.framework.utils;

import com.framework.constants.ErrorMessages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Ошибка при сериализации объекта в JSON", e);
            return "{}";
        }
    }

    public static JsonNode parse(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            log.error(ErrorMessages.UNKNOWN_ERROR, e.getMessage());
            throw new RuntimeException(ErrorMessages.UNKNOWN_ERROR.formatted(e.getMessage()), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Ошибка при десериализации JSON в {}", clazz.getSimpleName(), e);
            throw new RuntimeException("Ошибка при десериализации JSON", e);
        }
    }
} 