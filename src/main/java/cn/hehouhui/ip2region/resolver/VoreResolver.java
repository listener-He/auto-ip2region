package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Optional;

/**
 * 基于沃云查询
 *
 * @author HeHui
 * @date 2025-12-02 09:45
 */
public class VoreResolver extends AbstractNetworkIpSource {


    /**
     * 构造函数
     *
     * @param name               解析器名称
     * @param weight             解析器权重
     * @param permitsPerSecond   每秒许可数（限流速率）
     * @param httpRequestHandler HTTP请求处理器
     */
    public VoreResolver(double permitsPerSecond, String name, int weight, HttpRequestHandler httpRequestHandler) {
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
        String url = "https://api.vore.top/api/IPdata?ip=" + ip;
        String response = httpRequestHandler.get(url, 5000);
        if (response == null || response.isEmpty()) {
            return Optional.empty();
        }

        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        int code = jsonResponse.get("code").getAsInt();

        if (code != 200) {
            return Optional.empty();
        }

        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(jsonResponse.getAsJsonObject("ipinfo").get("text").getAsString());

        JsonObject ipData = jsonResponse.getAsJsonObject("ipdata");
        ipInfo.setCountry("中国"); // 根据文档示例，这个API主要提供中国IP信息
        ipInfo.setProvince(ipData.has("info1") && !ipData.get("info1").isJsonNull() ? ipData.get("info1").getAsString() : "");
        ipInfo.setCity(ipData.has("info2") && !ipData.get("info2").isJsonNull() ? ipData.get("info2").getAsString() : "");
        ipInfo.setIsp(ipData.has("isp") && !ipData.get("isp").isJsonNull() ? ipData.get("isp").getAsString() : "");

        // 尝试填充新字段
        JsonObject adcode = jsonResponse.getAsJsonObject("adcode");
        if (adcode != null) {
            if (adcode.has("a") && !adcode.get("a").isJsonNull()) {
                // 可以将行政编码存储在ASN字段中
                ipInfo.setAsn(adcode.get("a").getAsString());
            }
        }

        return Optional.of(ipInfo);
    }
}
