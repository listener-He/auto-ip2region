package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.fallback.FallbackStrategy;
import cn.hehouhui.ip2region.fallback.LocalFirstFallbackStrategy;
import cn.hehouhui.ip2region.loadbalancer.LoadBalancer;
import cn.hehouhui.ip2region.loadbalancer.WeightedLoadBalancer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IP查询引擎，统一入口，负责协调所有数据源、负载均衡和降级策略。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpQueryEngine {

    private final List<IpSource> sources;

    private final LoadBalancer loadBalancer;

    private final FallbackStrategy fallbackStrategy;

    private final Cache<String, IpInfo> cache;

    /**
     * 构造函数，使用默认的负载均衡器和降级策略
     *
     * @param sources IP数据源列表
     */
    public IpQueryEngine(List<IpSource> sources) {
        this(sources, new WeightedLoadBalancer(), new LocalFirstFallbackStrategy(), 1024, Duration.ofMinutes(10), Duration.ofMinutes(3));
    }

    /**
     * 构造函数，可自定义负载均衡器和降级策略
     *
     * @param sources           IP数据源列表
     * @param loadBalancer      负载均衡器
     * @param fallbackStrategy  降级策略
     * @param maxCacheSize      缓存最大容量
     * @param expireAfterWrite  缓存写入后多久过期
     * @param expireAfterAccess 缓存访问后多久过期
     */
    public IpQueryEngine(List<IpSource> sources, LoadBalancer loadBalancer, FallbackStrategy fallbackStrategy, int maxCacheSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        this.sources = new ArrayList<>(sources);
        this.loadBalancer = loadBalancer;
        this.fallbackStrategy = fallbackStrategy;

        // 初始化缓存
        this.cache = CacheBuilder.newBuilder()
            .maximumSize(maxCacheSize)
            .expireAfterAccess(expireAfterAccess)
            .expireAfterWrite(expireAfterWrite)
            .recordStats()
            .build();
    }

    public void addSource(IpSource source) {
        if (!sources.contains(source)) {
            sources.add(source);
        }
    }

    /**
     * 查询IP信息
     * <pre>
     * 该方法通过以下步骤查询IP信息：
     * 1. 首先尝试从缓存中获取结果，如果存在则直接返回
     * 2. 获取所有可用的数据源
     * 3. 使用负载均衡器选择最优的数据源
     * 4. 执行查询，如果成功则缓存结果并返回
     * 5. 如果查询失败，则根据降级策略尝试其他数据源
     * </pre>
     *
     * @param ip IP地址
     *
     * @return IP信息
     *
     * @throws Exception 查询异常，可能的异常包括：
     *                   - 当没有可用的数据源时抛出
     *                   - 当数据源查询过程中发生错误时抛出
     */
    public IpInfo query(String ip) throws Exception {
        if (ip == null || ip.isEmpty()) {
            return new IpInfo();
        }
        // 先尝试从缓存获取
        IpInfo cachedInfo = cache.getIfPresent(ip);
        if (cachedInfo != null) {
            return cachedInfo;
        }

        // 获取可用的数据源
        List<IpSource> availableSources = sources.stream()
            .filter(IpSource::isAvailable)
            .collect(Collectors.toList());

        if (availableSources.isEmpty()) {
            throw new Exception("No available IP sources");
        }

        // 使用负载均衡器选择数据源
        IpSource source = loadBalancer.select(availableSources);
        if (source == null) {
            throw new Exception("Failed to select IP source");
        }
        try {
            // 执行查询
            IpInfo info = source.query(ip);
            // 缓存结果
            if (source instanceof AbstractNetworkIpSource) {
                cache.put(ip, info);
            }
            return info;
        } catch (Exception e) {
            // 主数据源查询失败，尝试降级
            IpSource fallbackSource = fallbackStrategy.selectFallback(availableSources, source);
            if (fallbackSource != null) {
                try {
                    IpInfo info = fallbackSource.query(ip);
                    // 缓存结果
                    if (fallbackSource instanceof AbstractNetworkIpSource) {
                        cache.put(ip, info);
                    }
                    return info;
                } catch (Exception fallbackException) {
                    // 降级也失败，抛出原始异常
                    throw e;
                }
            } else {
                // 没有可用的降级数据源，抛出原始异常
                throw e;
            }
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getCacheStats() {
        return cache.stats().toString();
    }

    /**
     * 清空缓存
     */
    public void invalidateAllCache() {
        cache.invalidateAll();
    }

    /**
     * 清空指定IP的缓存
     *
     * @param ip IP地址
     */
    public void invalidateCache(String ip) {
        cache.invalidate(ip);
    }

    /**
     * 获取所有数据源
     *
     * @return 数据源列表
     */
    public List<IpSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    /**
     * 获取聚合指标
     *
     * @return 聚合指标
     */
    public AggregatedMetrics getAggregatedMetrics() {
        return AggregatedMetrics.fromSources(sources, cache.size(), getCacheStats());
    }
}
