package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

/**
 * 基于ip-moe查询
 *
 * @author HeHui
 * @date 2025-12-02
 */
public class IpMoeResolver extends AbstractNetworkIpSource {

    /**
     * 构造函数
     *
     * @param name               解析器名称
     * @param weight             解析器权重
     * @param permitsPerSecond   每秒许可数（限流速率）
     * @param httpRequestHandler HTTP请求处理器
     */
    public IpMoeResolver(String name, int weight, double permitsPerSecond, HttpRequestHandler httpRequestHandler) {
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
        String url = "https://ip-moe.zerodream.net/?ip=" + ip + "&iso";
        String response = httpRequestHandler.get(url, 5000);
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        int status = jsonResponse.get("status").getAsInt();

        if (status != 200) {
            return Optional.empty();
        }

        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(jsonResponse.get("addr").getAsString());
        ipInfo.setCountry(jsonResponse.get("country").getAsString());
        String city = jsonResponse.get("area").getAsString();
        ipInfo.setProvince("");
        if (city != null && city.contains("省")) {
            int index = city.indexOf("省");
            String province = city.substring(0, index + 1);
            ipInfo.setProvince(province);
            city = city.substring(index + 1);
        }
        ipInfo.setCity(city);
        ipInfo.setIsp(jsonResponse.get("provider").getAsString());

        // 尝试填充新字段
        if (jsonResponse.has("asn") && !jsonResponse.get("asn").isJsonNull()) {
            ipInfo.setAsn(jsonResponse.get("asn").getAsString());
        }

        if (jsonResponse.has("asn_owner") && !jsonResponse.get("asn_owner").isJsonNull()) {
            ipInfo.setAsnOwner(jsonResponse.get("asn_owner").getAsString());
        }

        if (jsonResponse.has("timezone") && !jsonResponse.get("timezone").isJsonNull()) {
            ipInfo.setTimezone(jsonResponse.get("timezone").getAsString());
        }

        if (jsonResponse.has("usage_type") && !jsonResponse.get("usage_type").isJsonNull()) {
            ipInfo.setUsageType(jsonResponse.get("usage_type").getAsString());
        }

        if (jsonResponse.has("risk") && !jsonResponse.get("risk").isJsonNull()) {
            ipInfo.setRisk(jsonResponse.get("risk").getAsString());
        }

        if (jsonResponse.has("proxy") && !jsonResponse.get("proxy").isJsonNull()) {
            ipInfo.setProxy(jsonResponse.get("proxy").getAsBoolean());
        }

        if (jsonResponse.has("crawler") && !jsonResponse.get("crawler").isJsonNull()) {
            ipInfo.setCrawlerName(jsonResponse.get("crawler").getAsString());
        }

        // 经纬度信息
        if (jsonResponse.has("longitude") && !jsonResponse.get("longitude").isJsonNull()) {
            ipInfo.setLongitude(jsonResponse.get("longitude").getAsDouble());
        }

        if (jsonResponse.has("latitude") && !jsonResponse.get("latitude").isJsonNull()) {
            ipInfo.setLatitude(jsonResponse.get("latitude").getAsDouble());
        }

        return Optional.of(ipInfo);
    }
}