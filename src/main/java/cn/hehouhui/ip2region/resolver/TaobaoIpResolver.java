package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

/**
 * 淘宝IP解析器，基于淘宝免费API实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class TaobaoIpResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数
     *
     * @param permitsPerSecond 每秒许可数（限流速率）
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public TaobaoIpResolver(double permitsPerSecond, String name, int weight) {
        super(name, weight, permitsPerSecond, new DefaultHttpRequestHandler());
    }

    /**
     * 构造函数
     *
     * @param permitsPerSecond   每秒许可数（限流速率）
     * @param name               解析器名称
     * @param weight             解析器权重
     * @param httpRequestHandler HTTP请求处理器
     */
    public TaobaoIpResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
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
        String urlString = "https://ip.taobao.com/outGetIpInfo?ip=" + ip + "&accessKey=alibaba-inc";
        String response = httpRequestHandler.get(urlString, 5000);

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        int code = jsonResponse.get("code").getAsInt();

        if (code != 0) {
            throw new Exception("API Error: " + jsonResponse.get("msg").getAsString());
        }

        JsonObject data = jsonResponse.getAsJsonObject("data");
        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(data.get("ip").getAsString());
        ipInfo.setCountry(data.has("country") && !data.get("country").isJsonNull() ? data.get("country").getAsString() : "");
        ipInfo.setProvince(data.has("region") && !data.get("region").isJsonNull() ? data.get("region").getAsString() : "");
        ipInfo.setCity(data.has("city") && !data.get("city").isJsonNull() ? data.get("city").getAsString() : "");
        ipInfo.setIsp(data.has("isp") && !data.get("isp").isJsonNull() ? data.get("isp").getAsString() : "");

        return Optional.of(ipInfo);
    }
}
