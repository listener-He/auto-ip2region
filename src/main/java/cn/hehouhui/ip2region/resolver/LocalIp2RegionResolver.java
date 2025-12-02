package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;

/**
 * 本地IP解析器，基于ip2region数据库实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class LocalIp2RegionResolver extends AbstractIpSource {
    private final Searcher searcher;

    /**
     * 构造函数
     *
     * @param searcher         ip2region搜索引擎
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public LocalIp2RegionResolver(Searcher searcher, String name, int weight) {
        super(name, weight);
        this.searcher = searcher;
    }

    @Override
    public IpInfo query(String ip) throws Exception {

        try {
            String region = searcher.search(ip);
            updateSuccessStats();
            IpInfo ipInfo = IpInfo.fromString(ip, region);
            
            // 本地数据库不提供新增字段信息，但为保持一致性保留默认值
            // ipInfo.setAsn(null);
            // ipInfo.setAsnOwner(null);
            // ipInfo.setLongitude(null);
            // ipInfo.setLatitude(null);
            // ipInfo.setTimezone(null);
            // ipInfo.setUsageType(null);
            // ipInfo.setNativeIp(null);
            // ipInfo.setRisk(null);
            // ipInfo.setProxy(null);
            // ipInfo.setCrawlerName(null);
            
            return ipInfo;
        } catch (Exception e) {
            updateFailureStats();
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        // 本地数据源始终可用
        return true;
    }

    /**
     * 关闭资源
     *
     * @throws IOException IO异常
     */
    public void close() throws IOException {
        if (searcher != null) {
            searcher.close();
        }
    }
}