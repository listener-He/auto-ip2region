package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;
import java.util.Objects;

/**
 * 聚合指标类，包含所有数据源的统计信息
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class AggregatedMetrics {
    /**
     * 本地数据源统计信息
     */
    private final DataSourceMetrics localMetrics;

    /**
     * 网络数据源统计信息
     */
    private final DataSourceMetrics networkMetrics;

    /**
     * 所有数据源统计信息
     */
    private final DataSourceMetrics totalMetrics;

    /**
     * 缓存条目数
     */
    private final long cacheSize;

    /**
     * 缓存统计信息
     */
    private final String cacheStats;

    /**
     * 构造函数
     *
     * @param localMetrics   本地数据源统计信息
     * @param networkMetrics 网络数据源统计信息
     * @param totalMetrics   所有数据源统计信息
     * @param cacheSize      缓存条目数
     * @param cacheStats     缓存统计信息
     */
    public AggregatedMetrics(DataSourceMetrics localMetrics, DataSourceMetrics networkMetrics,
                             DataSourceMetrics totalMetrics, long cacheSize, String cacheStats) {
        this.localMetrics = localMetrics;
        this.networkMetrics = networkMetrics;
        this.totalMetrics = totalMetrics;
        this.cacheSize = cacheSize;
        this.cacheStats = cacheStats;
    }

    /**
     * 从数据源列表创建聚合指标
     *
     * @param sources    数据源列表
     * @param cacheSize  缓存条目数
     * @param cacheStats 缓存统计信息
     * @return 聚合指标
     */
    public static AggregatedMetrics fromSources(List<IpSource> sources, long cacheSize, String cacheStats) {
        DataSourceMetrics localMetrics = new DataSourceMetrics();
        DataSourceMetrics networkMetrics = new DataSourceMetrics();

        for (IpSource source : sources) {
            // 根据数据源类型分别统计
            if (source instanceof AbstractNetworkIpSource networkSource) {
                // 网络数据源特有的统计
                networkMetrics.executionCount += source.getExecutionCount();
                networkMetrics.failureCount += source.getFailureCount();

                // 注意：这里我们不能简单地累加平均响应时间，而应该累加总响应时间
                // 但由于AbstractNetworkIpSource只提供了平均响应时间，我们需要做一些近似计算
                if (source.getExecutionCount() > 0) {
                    networkMetrics.totalResponseTime += networkSource.getAverageResponseTime() * source.getExecutionCount();
                    networkMetrics.responseCount += source.getExecutionCount();
                }
            } else {
                localMetrics.executionCount += source.getExecutionCount();
                localMetrics.failureCount += source.getFailureCount();
            }
        }

        // 计算总计统计
        DataSourceMetrics totalMetrics = new DataSourceMetrics();
        totalMetrics.executionCount = localMetrics.executionCount + networkMetrics.executionCount;
        totalMetrics.failureCount = localMetrics.failureCount + networkMetrics.failureCount;
        totalMetrics.totalResponseTime = networkMetrics.totalResponseTime; // 只有网络数据源有响应时间
        totalMetrics.responseCount = networkMetrics.responseCount; // 只有网络数据源有响应次数

        return new AggregatedMetrics(localMetrics, networkMetrics, totalMetrics, cacheSize, cacheStats);
    }

    public DataSourceMetrics getLocalMetrics() {
        return localMetrics;
    }

    public DataSourceMetrics getNetworkMetrics() {
        return networkMetrics;
    }

    public DataSourceMetrics getTotalMetrics() {
        return totalMetrics;
    }

    public long getCacheSize() {
        return cacheSize;
    }

    public String getCacheStats() {
        return cacheStats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedMetrics that = (AggregatedMetrics) o;
        return cacheSize == that.cacheSize &&
                Objects.equals(localMetrics, that.localMetrics) &&
                Objects.equals(networkMetrics, that.networkMetrics) &&
                Objects.equals(totalMetrics, that.totalMetrics) &&
                Objects.equals(cacheStats, that.cacheStats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localMetrics, networkMetrics, totalMetrics, cacheSize, cacheStats);
    }

    @Override
    public String toString() {
        return "AggregatedMetrics{" +
                "localMetrics=" + localMetrics +
                ", networkMetrics=" + networkMetrics +
                ", totalMetrics=" + totalMetrics +
                ", cacheSize=" + cacheSize +
                ", cacheStats='" + cacheStats + '\'' +
                '}';
    }

    /**
     * 数据源统计信息类
     */
    public static class DataSourceMetrics {
        /**
         * 执行次数
         */
        long executionCount = 0;

        /**
         * 失败次数
         */
        long failureCount = 0;

        /**
         * 总响应时间（仅网络数据源）
         */
        double totalResponseTime = 0.0;

        /**
         * 响应次数（仅网络数据源）
         */
        long responseCount = 0;

        /**
         * 获取成功率
         *
         * @return 成功率 (0.0 - 1.0)
         */
        public double getSuccessRate() {
            if (executionCount == 0) {
                return 1.0;
            }
            return (double) (executionCount - failureCount) / executionCount;
        }

        /**
         * 获取平均响应时间（仅网络数据源）
         *
         * @return 平均响应时间（毫秒）
         */
        public double getAverageResponseTime() {
            if (responseCount == 0) {
                return 0.0;
            }
            return totalResponseTime / responseCount;
        }

        public long getExecutionCount() {
            return executionCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public double getTotalResponseTime() {
            return totalResponseTime;
        }

        public long getResponseCount() {
            return responseCount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataSourceMetrics that = (DataSourceMetrics) o;
            return executionCount == that.executionCount &&
                    failureCount == that.failureCount &&
                    Double.compare(that.totalResponseTime, totalResponseTime) == 0 &&
                    responseCount == that.responseCount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(executionCount, failureCount, totalResponseTime, responseCount);
        }

        @Override
        public String toString() {
            return "DataSourceMetrics{" +
                    "executionCount=" + executionCount +
                    ", failureCount=" + failureCount +
                    ", successRate=" + String.format("%.2f", getSuccessRate()) +
                    ", totalResponseTime=" + String.format("%.2f", totalResponseTime) +
                    ", responseCount=" + responseCount +
                    ", averageResponseTime=" + String.format("%.2f", getAverageResponseTime()) +
                    '}';
        }
    }
}
