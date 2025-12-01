package cn.hehouhui.ip2region.fallback;

import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;

/**
 * 本地优先降级策略，优先选择本地数据源作为降级选项。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class LocalFirstFallbackStrategy implements FallbackStrategy {

    @Override
    public IpSource selectFallback(List<IpSource> sources, IpSource primarySource) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }

        // 优先选择本地数据源
        for (IpSource source : sources) {
            if (source != primarySource && source.isAvailable() && isLocalSource(source)) {
                return source;
            }
        }

        // 如果没有本地数据源，则选择其他可用的数据源
        for (IpSource source : sources) {
            if (source != primarySource && source.isAvailable()) {
                return source;
            }
        }

        return null;
    }

    /**
     * 判断是否为本地数据源
     *
     * @param source 数据源
     * @return 是否为本地数据源
     */
    private boolean isLocalSource(IpSource source) {
        String name = source.getName().toLowerCase();
        return name.contains("local") || name.contains("ip2region") || name.contains("database");
    }
}