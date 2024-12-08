package com.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final Properties properties = new Properties();
    private static ConfigurationManager instance;

    private ConfigurationManager() {
        loadProperties();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadProperties() {
        String environment = System.getProperty("env", "config");
        String propertiesFile = environment + ".properties";
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                log.error("Не удалось найти файл {}", propertiesFile);
                throw new RuntimeException("Файл конфигурации не найден: " + propertiesFile);
            }
            properties.load(input);
            log.info("Загружена конфигурация из файла: {}", propertiesFile);
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла конфигурации", e);
            throw new RuntimeException("Ошибка при загрузке конфигурации", e);
        }
    }

    public String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public String getWebSocketUrl() {
        return getProperty("ws.base.url");
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(getProperty("timeout.connection", "5000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(getProperty("timeout.read", "10000"));
    }

    private String getProperty(String key) {
        return getProperty(key, null);
    }

    private String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key, defaultValue);
        }
        if (value == null) {
            log.error("Не найдено значение для ключа: {}", key);
            throw new RuntimeException("Отсутствует обязательный параметр конфигурации: " + key);
        }
        return value;
    }
} 