package cn.hehouhui.ip2region.fallback;

import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;

/**
 * 降级策略接口，定义了当主数据源不可用时如何选择备用数据源。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public interface FallbackStrategy {

    /**
     * 当主数据源不可用时，选择降级数据源
     *
     * @param sources       所有数据源
     * @param primarySource 主数据源
     * @return 降级数据源
     */
    IpSource selectFallback(List<IpSource> sources, IpSource primarySource);
}