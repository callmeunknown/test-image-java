# Todo API Test Framework

Фреймворк для автоматизированного тестирования REST API и WebSocket сервиса управления задачами (Todo).

## Структура проекта

```
src
├── main/java/com/framework
│ ├── client
│ │ └── RestClient.java # HTTP клиент на базе RestAssured
│ ├── config
│ │ ├── AuthConfig.java # Конфигурация авторизации
│ │ └── ConfigurationManager.java # Управление конфигурацией
│ ├── constants
│ │ └── ErrorMessages.java # Константы сообщений об ошибках
│ ├── models
│ │ └── Todo.java # Модель данных задачи
│ ├── utils
│ │ ├── JsonUtil.java # Утилиты для работы с JSON
│ │ └── LoggerUtil.java # Утилиты логирования
│ └── websocket
│ └── TodoWebSocketClient.java # WebSocket клиент
│
└── test/java/com/framework
│ └── rest # REST API тесты
│ └──  DeleteTest.java
│ └──  GetTest.java
│ └──  PostTest.java
│ └──  PutTest.java
└── websocket # WebSocket тесты
│ └── WebSocketTest.java
```