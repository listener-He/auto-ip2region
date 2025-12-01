package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Optional;

/**
 * IPInfo解析器，基于ipinfo.io API实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpInfoResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数
     *
     * @param permitsPerSecond 每秒许可数（限流速率）
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public IpInfoResolver(double permitsPerSecond, String name, int weight) {
        super(name, weight, permitsPerSecond, new DefaultHttpRequestHandler());
    }

    /**
     * 构造函数
     *
     * @param permitsPerSecond    每秒许可数（限流速率）
     * @param name                解析器名称
     * @param weight              解析器权重
     * @param httpRequestHandler  HTTP请求处理器
     */
    public IpInfoResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
        super(name, weight, permitsPerSecond, httpRequestHandler);
    }

    /**
     * 发送请求并解析IP信息
     *
     * @param ip IP地址
     *
     * @return IP信息
     *
     * @throws Exception 请求异常
     */
    @Override
    protected Optional<IpInfo> request(String ip) throws Exception {
        String urlString = "http://ipinfo.io/" + ip + "/json";
        String response = httpRequestHandler.get(urlString, 5000);
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(jsonResponse.has("ip") && !jsonResponse.get("ip").isJsonNull() ? jsonResponse.get("ip").getAsString() : "");
        ipInfo.setCountry(jsonResponse.has("country") && !jsonResponse.get("country").isJsonNull() ? jsonResponse.get("country").getAsString() : "");
        ipInfo.setProvince(jsonResponse.has("region") && !jsonResponse.get("region").isJsonNull() ? jsonResponse.get("region").getAsString() : "");
        ipInfo.setCity(jsonResponse.has("city") && !jsonResponse.get("city").isJsonNull() ? jsonResponse.get("city").getAsString() : "");
        ipInfo.setIsp(jsonResponse.has("org") && !jsonResponse.get("org").isJsonNull() ? jsonResponse.get("org").getAsString() : "");

        return Optional.of(ipInfo);
    }
}
