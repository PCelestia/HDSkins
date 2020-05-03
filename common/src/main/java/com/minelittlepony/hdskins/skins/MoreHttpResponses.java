package com.minelittlepony.hdskins.skins;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Utility class for getting different response types from a http response.
 */
@FunctionalInterface
public interface MoreHttpResponses extends AutoCloseable {

    CloseableHttpResponse response();

    default boolean ok() {
        return responseCode() < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    default void checkContent(String targetType, Callable<String> errorMessage) throws IOException {
        if (!targetType.contentEquals(contentType().getMimeType())) {
            String msg;
            try {
                msg = errorMessage.call();
            } catch (Exception e) {
                throw new IOException(e);
            }
            throw new IOException(msg);
        }
    }

    default int responseCode() {
        return response().getStatusLine().getStatusCode();
    }

    default Optional<HttpEntity> entity() {
        return Optional.ofNullable(response().getEntity());
    }

    default ContentType contentType() {
        return entity()
                .map(ContentType::get)
                .orElse(ContentType.DEFAULT_TEXT);
    }

    default InputStream content() throws IOException {
        return response().getEntity().getContent();
    }

    default BufferedReader reader() throws IOException {
        return new BufferedReader(new InputStreamReader(content(), StandardCharsets.UTF_8));
    }

    default byte[] bytes() throws IOException {
        try (InputStream input = content()) {
            return ByteStreams.toByteArray(input);
        }
    }

    default String text() throws IOException {
        try (BufferedReader reader = reader()) {
            return CharStreams.toString(reader);
        }
    }

    default <T> T json(Gson gson, Class<T> type) throws IOException {
        return json(gson, (Type) type);
    }

    default <T> T json(Gson gson, Type type) throws IOException {
        checkContent("application/json", () -> "Non-json content! content: " + text());
        String json = text();
        try {
            return gson.fromJson(json, type);
        } catch (JsonParseException e) {
            LogManager.getLogger().warn("Failed to parse json. Actual text: {}", json);
            throw e;
        }
    }

    @Override
    default void close() throws IOException {
        response().close();
    }

    static MoreHttpResponses execute(CloseableHttpClient client, HttpUriRequest request) throws IOException {
        CloseableHttpResponse response = client.execute(request);
        return () -> response;
    }

    static NameValuePair[] mapAsParameters(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry ->
                        new BasicNameValuePair(entry.getKey(), entry.getValue())
                )
                .toArray(NameValuePair[]::new);
    }
}
