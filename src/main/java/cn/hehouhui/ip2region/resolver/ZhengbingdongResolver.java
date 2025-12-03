package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

/**
 *  IP解析器，基于<a href="https://ip.zhengbingdong.com/">Zhengbingdong</a>免费API实现。
 *
 * 免费使用限制：每个IP的访问频率为 600次/分钟。
 *
 * @author HeHui
 * @date 2025-12-03
 */
public class ZhengbingdongResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数，默认每秒允许10个请求（600次/分钟）
     *
     * @param name   解析器名称
     * @param weight 解析器权重
     */
    public ZhengbingdongResolver(String name, int weight) {
        super(name, weight, 10.0, new DefaultHttpRequestHandler());
    }

    /**
     * 构造函数，可自定义限流速率
     *
     * @param name                解析器名称
     * @param weight              解析器权重
     * @param permitsPerSecond    每秒许可数（限流速率）
     * @param httpRequestHandler  HTTP请求处理器
     */
    public ZhengbingdongResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
        super(name, weight, permitsPerSecond, httpRequestHandler);
    }

    /**
     * 发送请求并解析IP信息
     *
     * @param ip IP地址
     * @return IP信息
     * @throws Exception 请求异常
     */
    @Override
    protected Optional<IpInfo> request(String ip) throws Exception {
        String urlString = "https://ip.zhengbingdong.com/v1/get?ip=" + ip;
        String response = httpRequestHandler.get(urlString, 5000);

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

        // 检查响应状态码
        if (!jsonResponse.has("ret") || jsonResponse.get("ret").getAsInt() != 200) {
            throw new Exception("API Error: Invalid response status");
        }

        // 获取data对象
        if (!jsonResponse.has("data") || jsonResponse.get("data").isJsonNull()) {
            return Optional.empty();
        }

        JsonObject data = jsonResponse.getAsJsonObject("data");

        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(data.has("ip") && !data.get("ip").isJsonNull() ? data.get("ip").getAsString() : "");
        ipInfo.setCountry(data.has("country") && !data.get("country").isJsonNull() ? data.get("country").getAsString() : "");
        ipInfo.setProvince(data.has("prov") && !data.get("prov").isJsonNull() ? data.get("prov").getAsString() : "");
        ipInfo.setCity(data.has("city") && !data.get("city").isJsonNull() ? data.get("city").getAsString() : "");
        ipInfo.setIsp(data.has("isp") && !data.get("isp").isJsonNull() ? data.get("isp").getAsString() : "");

        // 填充新增字段
        if (data.has("country_code") && !data.get("country_code").isJsonNull()) {
            ipInfo.setAsn(data.get("country_code").getAsString());
        }

        if (data.has("lng") && !data.get("lng").isJsonNull()) {
            try {
                ipInfo.setLongitude(Double.parseDouble(data.get("lng").getAsString()));
            } catch (NumberFormatException ignored) {
                // 忽略解析错误
            }
        }

        if (data.has("lat") && !data.get("lat").isJsonNull()) {
            try {
                ipInfo.setLatitude(Double.parseDouble(data.get("lat").getAsString()));
            } catch (NumberFormatException ignored) {
                // 忽略解析错误
            }
        }

        if (data.has("area") && !data.get("area").isJsonNull()) {
            ipInfo.setRegion(data.get("area").getAsString());
        }

        return Optional.of(ipInfo);
    }
}
