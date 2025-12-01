package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Optional;

/**
 * XXLB解析器，基于ipapi.xxlb.org API实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class XxlbResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数
     *
     * @param permitsPerSecond 每秒许可数（限流速率）
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public XxlbResolver(double permitsPerSecond, String name, int weight) {
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
    public XxlbResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
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
        String urlString = "https://ipapi.xxlb.org/?ip=" + ip;
        String response = httpRequestHandler.get(urlString, 5000);
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(jsonResponse.has("ip") && !jsonResponse.get("ip").isJsonNull() ? jsonResponse.get("ip").getAsString() : "");
        ipInfo.setCountry(jsonResponse.has("country") && jsonResponse.getAsJsonObject("country").has("name")
            && !jsonResponse.getAsJsonObject("country").get("name").isJsonNull()
            ? jsonResponse.getAsJsonObject("country").get("name").getAsString() : "");

        // 处理地区信息
        if (jsonResponse.has("regions") && !jsonResponse.get("regions").isJsonNull()) {
            JsonArray regions = jsonResponse.getAsJsonArray("regions");
            if (!regions.isEmpty()) {
                ipInfo.setProvince(regions.get(0).getAsString());
            }
            if (regions.size() > 1) {
                ipInfo.setCity(regions.get(1).getAsString());
            }
        }

        // 处理ISP信息
        if (jsonResponse.has("as") && !jsonResponse.getAsJsonObject("as").get("info").isJsonNull()) {
            ipInfo.setIsp(jsonResponse.getAsJsonObject("as").get("info").getAsString());
        }

        return Optional.of(ipInfo);
    }
}
