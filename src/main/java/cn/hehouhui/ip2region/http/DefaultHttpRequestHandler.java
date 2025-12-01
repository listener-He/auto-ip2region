package cn.hehouhui.ip2region.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 默认HTTP请求处理器实现，基于JDK内置的HttpClient
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class DefaultHttpRequestHandler implements HttpRequestHandler {

    private final HttpClient httpClient;

    /**
     * 构造函数，创建默认的HTTP客户端
     */
    public DefaultHttpRequestHandler() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    /**
     * 构造函数，使用自定义的HTTP客户端
     *
     * @param httpClient HTTP客户端
     */
    public DefaultHttpRequestHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String get(String url, int timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeout))
            .header("User-Agent", "IP2Region Client/1.0")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Override
    public String post(String url, String body, int timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeout))
            .header("User-Agent", "IP2Region Client/1.0")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
