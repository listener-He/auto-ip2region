package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.core.IpSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
     *
     * @return 聚合指标
     */
    public static AggregatedMetrics fromSources(List<IpSource> sources, long cacheSize, String cacheStats) {
        DataSourceMetrics localMetrics = new DataSourceMetrics();
        DataSourceMetrics networkMetrics = new DataSourceMetrics();
        List<SourceMetrics> sourceMetrics = new ArrayList<>();
        for (IpSource source : sources) {
            // 根据数据源类型分别统计
            if (source instanceof AbstractNetworkIpSource networkSource) {
                networkMetrics.executionCount += source.getExecutionCount();
                networkMetrics.failureCount += source.getFailureCount();
                // 注意：这里我们不能简单地累加平均响应时间，而应该累加总响应时间
                networkMetrics.totalResponseTime += networkSource.getTotalResponseTime().get();
                networkMetrics.responseCount += source.getExecutionCount();
                sourceMetrics.add(new SourceMetrics(networkSource.getName(), networkSource.getWeight(), networkSource.getSuccessRate(), networkSource.getExecutionCount(), networkSource.getFailureCount(), networkSource.getTotalResponseTime().get(), networkSource.getResponseCount().get()));
            } else {
                localMetrics.executionCount += source.getExecutionCount();
                localMetrics.failureCount += source.getFailureCount();
            }
        }
        networkMetrics.setAllSources(sourceMetrics);
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
         * 所有数据源
         */
        private List<SourceMetrics> allSources;

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

        public List<SourceMetrics> getAllSources() {
            return allSources;
        }

        public void setAllSources(List<SourceMetrics> allSources) {
            this.allSources = allSources;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataSourceMetrics that = (DataSourceMetrics) o;
            return executionCount == that.executionCount &&
                failureCount == that.failureCount &&
                Double.compare(that.totalResponseTime, totalResponseTime) == 0 &&
                responseCount == that.responseCount &&
                Objects.equals(allSources, that.allSources);
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
                ", allSources=" + (allSources == null ? "[]" : "[" + allSources.stream().map(SourceMetrics::toString).collect(Collectors.joining(",")) + "]") +
                '}';
        }
    }


    /**
     * 数据源统计信息类
     */
    public static class SourceMetrics {
        /**
         * 数据源名称
         */
        private String name;
        /**
         * 数据源权重
         */
        private int weight;

        /**
         * 数据源成功率
         */
        private double successRate;


        /**
         * 数据源执行次数
         */
        private long executionCount;
        /**
         * 数据源失败次数
         */
        private long failureCount;
        /**
         * 数据源总响应时间（毫秒）
         */
        private Long totalResponseTime;

        /**
         * 数据源响应次数
         */
        private Long responseCount;


        public SourceMetrics() {
        }

        public SourceMetrics(String name, int weight, double successRate, long executionCount, long failureCount, Long totalResponseTime, Long responseCount) {
            this.name = name;
            this.weight = weight;
            this.successRate = successRate;
            this.executionCount = executionCount;
            this.failureCount = failureCount;
            this.totalResponseTime = totalResponseTime;
            this.responseCount = responseCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }


        public long getExecutionCount() {
            return executionCount;
        }

        public void setExecutionCount(long executionCount) {
            this.executionCount = executionCount;
        }

        public long getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(long failureCount) {
            this.failureCount = failureCount;
        }

        public Long getTotalResponseTime() {
            return totalResponseTime;
        }

        public void setTotalResponseTime(Long totalResponseTime) {
            this.totalResponseTime = totalResponseTime;
        }

        public Long getResponseCount() {
            return responseCount;
        }

        public void setResponseCount(Long responseCount) {
            this.responseCount = responseCount;
        }


        @Override
        public String toString() {
            return "SourceMetrics{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", successRate=" + successRate +
                ", executionCount=" + executionCount +
                ", failureCount=" + failureCount +
                ", totalResponseTime=" + totalResponseTime +
                ", responseCount=" + responseCount +
                '}';
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof SourceMetrics that)) return false;

            return getWeight() == that.getWeight() && Double.compare(getSuccessRate(), that.getSuccessRate()) == 0 && getExecutionCount() == that.getExecutionCount() && getFailureCount() == that.getFailureCount() && getName().equals(that.getName()) && Objects.equals(getTotalResponseTime(), that.getTotalResponseTime()) && Objects.equals(getResponseCount(), that.getResponseCount());
        }

        @Override
        public int hashCode() {
            int result = getName().hashCode();
            result = 31 * result + getWeight();
            result = 31 * result + Double.hashCode(getSuccessRate());
            result = 31 * result + Long.hashCode(getExecutionCount());
            result = 31 * result + Long.hashCode(getFailureCount());
            result = 31 * result + Objects.hashCode(getTotalResponseTime());
            result = 31 * result + Objects.hashCode(getResponseCount());
            return result;
        }
    }
}
