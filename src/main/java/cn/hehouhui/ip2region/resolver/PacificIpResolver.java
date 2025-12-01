package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

/**
 * Pacific网络IP解析器，基于whois.pconline.com.cn API实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class PacificIpResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数
     *
     * @param permitsPerSecond 每秒许可数（限流速率）
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public PacificIpResolver(double permitsPerSecond, String name, int weight) {
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
    public PacificIpResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
        super(name, weight, permitsPerSecond, httpRequestHandler);
    }

    @Override
    public IpInfo query(String ip) throws Exception {
        double waitTime = rateLimiter.acquire();
        updateAcquireTimeStats(waitTime);

        try {
            String urlString = "http://whois.pconline.com.cn/ipJson.jsp?ip=" + ip + "&json=true";
            String response = httpRequestHandler.get(urlString, 5000);

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp(jsonResponse.has("ip") && !jsonResponse.get("ip").isJsonNull() ? jsonResponse.get("ip").getAsString() : "");
            ipInfo.setCountry("中国"); // 该API主要针对中国IP
            ipInfo.setProvince(jsonResponse.has("pro") && !jsonResponse.get("pro").isJsonNull() ? jsonResponse.get("pro").getAsString() : "");
            ipInfo.setCity(jsonResponse.has("city") && !jsonResponse.get("city").isJsonNull() ? jsonResponse.get("city").getAsString() : "");
            ipInfo.setIsp(jsonResponse.has("addr") && !jsonResponse.get("addr").isJsonNull() ? jsonResponse.get("addr").getAsString() : "");

            updateSuccessStats();
            return ipInfo;
        } catch (IOException | InterruptedException e) {
            updateFailureStats();
            throw new Exception("Network error occurred", e);
        } catch (Exception e) {
            updateFailureStats();
            throw e;
        }
    }
}
