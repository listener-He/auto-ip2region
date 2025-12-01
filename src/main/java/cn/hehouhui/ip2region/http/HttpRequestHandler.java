package cn.hehouhui.ip2region.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP请求处理器接口，用于处理网络请求
 *
 * @author HeHui
 * @date 2025-12-01
 */
public interface HttpRequestHandler {

    /**
     * 发送HTTP GET请求
     *
     * @param url     请求URL
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    String get(String url, int timeout) throws IOException, InterruptedException;

    /**
     * 发送HTTP POST请求
     *
     * @param url     请求URL
     * @param body    请求体
     * @param timeout 超时时间（毫秒）
     * @return 响应字符串
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    String post(String url, String body, int timeout) throws IOException, InterruptedException;


}
