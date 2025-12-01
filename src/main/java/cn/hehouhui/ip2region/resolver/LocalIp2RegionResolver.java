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
     * @param permitsPerSecond 每秒许可数（限流速率）
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public LocalIp2RegionResolver(Searcher searcher, double permitsPerSecond, String name, int weight) {
        super(name, weight, permitsPerSecond);
        this.searcher = searcher;
    }

    @Override
    public IpInfo query(String ip) throws Exception {
        // Acquire permit from rate limiter (blocking if necessary)
        double waitTime = rateLimiter.acquire();
        updateAcquireTimeStats(waitTime);

        try {
            String region = searcher.search(ip);
            updateSuccessStats();
            return IpInfo.fromString(ip, region);
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