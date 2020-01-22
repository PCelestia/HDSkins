package com.minelittlepony.hdskins.util.net;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.minelittlepony.hdskins.client.HDSkins;
import com.minelittlepony.hdskins.skins.SkinServer;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility class for getting different response types from a http response.
 */
@FunctionalInterface
public interface MoreHttpResponses extends AutoCloseable {

    CloseableHttpResponse getResponse();

    default boolean ok() {
        return responseCode() == HttpStatus.SC_OK;
    }

    default boolean json() {
        return "application/json".equals(contentType());
    }

    default int responseCode() {
        return getResponse().getStatusLine().getStatusCode();
    }

    default String contentType() {
        return getResponse().getEntity().getContentType().getValue();
    }

    default InputStream inputStream() throws IOException {
        return getResponse().getEntity().getContent();
    }

    default BufferedReader reader() throws IOException {
        return new BufferedReader(new InputStreamReader(inputStream(), StandardCharsets.UTF_8));
    }

    default byte[] bytes() throws IOException {
        try (InputStream input = inputStream()) {
            return ByteStreams.toByteArray(input);
        }
    }

    default String text() throws IOException {
        try (BufferedReader reader = reader()) {
            return CharStreams.toString(reader);
        }
    }

    default Stream<String> lines() throws IOException {
        try (BufferedReader reader = reader()) {
            return reader.lines();
        }
    }

    default <T> T json(Class<T> type, String errorMessage) throws IOException {
        return json((Type)type, errorMessage);
    }

    default <T> T json(Type type, String errorMessage) throws IOException {
        if (!json()) {
            String text = text();
            HDSkins.logger.error(errorMessage, text);
            throw new IOException(text);
        }

        try (BufferedReader reader = reader()) {
            return SkinServer.gson.fromJson(reader, type);
        }
    }

    default <T> T unwrapAsJson(Type type) throws IOException {
        if (ok()) {
            return json(type, "Server returned a non-json response!");
        }

        throw exception();
    }

    default IOException exception() throws IOException {
        return new IOException(json(JsonObject.class, "Server error wasn't in json: {}").get("message").getAsString());
    }

    @Override
    default void close() throws IOException {
        this.getResponse().close();
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
