package cn.hehouhui.ip2region.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

/**
 * 默认HTTP请求处理器实现，基于JDK内置的HttpClient
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class DefaultHttpRequestHandler implements HttpRequestHandler {

    private final HttpClient httpClient;

    private final Random random = new Random();

    // 真实的User-Agent列表
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Edge/131.0.2903.86",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Android 14; Mobile; rv:133.0) Gecko/133.0 Firefox/133.0",
        "Mozilla/5.0 (iPad; CPU OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko"
    };



    // 常用的Accept-Language头
    private static final String[] ACCEPT_LANGUAGE_HEADERS = {
        "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
        "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7",
        "zh-CN,zh;q=0.9,en;q=0.8",
        "en-US,en;q=0.9",
        "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7"
    };

    // 常用的Accept-Encoding头
    private static final String[] ACCEPT_ENCODING_HEADERS = {
        "gzip, deflate, br",
        "gzip, deflate",
        "br, gzip",
        "identity"
    };

    // 常用的Cache-Control头
    private static final String[] CACHE_CONTROL_HEADERS = {
        "no-cache",
        "max-age=30",
        "no-store",
        "no-cache, no-store, must-revalidate"
    };

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
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeout))
            .GET();

        // 随机添加请求头
        addRandomHeaders(requestBuilder);

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    @Override
    public String post(String url, String body, int timeout) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMillis(timeout))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));

        // 随机添加请求头
        addRandomHeaders(requestBuilder);

        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 随机添加请求头
     * @param builder 请求构建器
     */
    private void addRandomHeaders(HttpRequest.Builder builder) {
        builder.header("User-Agent", USER_AGENTS[random.nextInt(USER_AGENTS.length)])
               .header("Accept-Language", ACCEPT_LANGUAGE_HEADERS[random.nextInt(ACCEPT_LANGUAGE_HEADERS.length)])
               .header("Accept-Encoding", ACCEPT_ENCODING_HEADERS[random.nextInt(ACCEPT_ENCODING_HEADERS.length)]);

        // 随机决定是否添加Cache-Control头
        if (random.nextBoolean()) {
            builder.header("Cache-Control", CACHE_CONTROL_HEADERS[random.nextInt(CACHE_CONTROL_HEADERS.length)]);
        }

        // 随机决定是否添加Upgrade-Insecure-Requests头
        if (random.nextBoolean()) {
            builder.header("Upgrade-Insecure-Requests", "1");
        }

        // 随机决定是否添加DNT头
        if (random.nextBoolean()) {
            builder.header("DNT", "1");
        }

        // 随机决定是否添加Sec-GPC头
        if (random.nextBoolean()) {
            builder.header("Sec-GPC", "1");
        }
    }
}
