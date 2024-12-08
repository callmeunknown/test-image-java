package com.framework.client;

import com.framework.config.ConfigurationManager;
import com.framework.utils.LoggerUtil;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class RestClient {
    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private final ConfigurationManager config;

    public static class RequestBuilder {
        private final RequestSpecification spec;
        private final String endpoint;
        private final String method;
        private Object requestBody;

        private RequestBuilder(String method, String endpoint) {
            this.method = method;
            this.endpoint = endpoint;
            this.spec = given()
                    .contentType(ContentType.JSON)
                    .config(RestAssured.config()
                            .connectionConfig(RestAssured.config().getConnectionConfig()
                                    .closeIdleConnectionsAfterEachResponse()));
        }

        public RequestBuilder header(String name, String value) {
            spec.header(name, value);
            return this;
        }

        public RequestBuilder body(Object body) {
            this.requestBody = body;
            spec.body(body);
            return this;
        }

        public Response execute() {
            LoggerUtil.logRequest(method, endpoint, requestBody);
            try {
                Response response = switch (method) {
                    case "DELETE" -> spec.delete(endpoint);
                    case "GET" -> spec.get(endpoint);
                    case "POST" -> spec.post(endpoint);
                    case "PUT" -> spec.put(endpoint);
                    default -> throw new IllegalArgumentException("Неподдерживаемый метод: " + method);
                };
                LoggerUtil.logResponse(response, method, endpoint);
                return response;
            } catch (Exception e) {
                log.error("Ошибка при выполнении {} запроса к {}: {}",
                        method, endpoint, e.getMessage());
                throw e;
            }
        }
    }

    public RestClient() {
        this.config = ConfigurationManager.getInstance();
        RestAssured.baseURI = config.getApiBaseUrl();
        RestAssured.config = RestAssured.config()
                .connectionConfig(RestAssured.config().getConnectionConfig()
                        .closeIdleConnectionsAfterEachResponse());
    }

    public RequestBuilder delete(String endpoint) {
        return new RequestBuilder("DELETE", endpoint);
    }

    public RequestBuilder get(String endpoint) {
        return new RequestBuilder("GET", endpoint);
    }

    public RequestBuilder post(String endpoint, Object body) {
        return new RequestBuilder("POST", endpoint).body(body);
    }

    public RequestBuilder put(String endpoint, Object body) {
        return new RequestBuilder("PUT", endpoint).body(body);
    }
} 