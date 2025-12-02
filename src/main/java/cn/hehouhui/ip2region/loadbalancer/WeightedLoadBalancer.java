package cn.hehouhui.ip2region.loadbalancer;

import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;

/**
 * 基于权重、执行次数、成功率和限流状况的加权负载均衡器。
 * 算法：score = weight * 0.4 + successRate * 0.25 + (1 - (executionCount / maxExecutionCount)) * 0.2 + availableRate * 0.15
 * 其中：
 * - weight: 权重占比40%
 * - successRate: 成功率占比25%
 * - executionCount: 执行次数占比20%（执行次数越少优先级越高，实现请求均匀分布）
 * - availableRate: 可用性占比15%（基于限流器的可用性评估）
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class WeightedLoadBalancer implements LoadBalancer {


    /**
     * 从数据源列表中根据加权负载均衡算法选择最优的数据源
     * 算法综合考虑权重、成功率、执行次数和可用性四个维度
     *
     * @param sources 数据源列表
     * @return 选中的数据源，如果列表为空则返回null
     */
    @Override
    public IpSource select(List<IpSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }

        if (sources.size() == 1) {
            return sources.getFirst();
        }

        // 计算所有数据源中的最大执行次数，用于负载均衡计算
        long maxExecutionCount = sources.stream()
            .mapToLong(IpSource::getExecutionCount)
            .max()
            .orElse(0);


        IpSource bestSource = sources.getFirst();
        double bestScore = calculateScore(bestSource, maxExecutionCount);

        // 遍历所有数据源，找出得分最高的
        for (int i = 1; i < sources.size(); i++) {
            IpSource source = sources.get(i);
            double score = calculateScore(source, maxExecutionCount);
            if (score > bestScore) {
                bestScore = score;
                bestSource = source;
            }
        }

        return bestSource;
    }

    /**
     * 计算数据源得分
     * 算法：score = weight * 0.4 + successRate * 0.25 + (1 - (executionCount / maxExecutionCount)) * 0.2 + availableRate * 0.15
     *
     * @param source            数据源
     * @param maxExecutionCount 最大执行次数
     *
     * @return 得分
     */
    private double calculateScore(IpSource source, long maxExecutionCount) {
        // 权重占比40%
        double weightScore = normalizeWeight(source.getWeight()) * 0.4;

        // 成功率占比25%
        double successRateScore = source.getSuccessRate() * 0.25;

        // 执行次数占比20%（执行次数越少优先级越高）
        double executionCountScore = 0;
        if (maxExecutionCount > 0) {
            if (source.getExecutionCount() <= 0) {
                executionCountScore = 1.0;
            } else {
                double executionCountRatio = (double) source.getExecutionCount() / maxExecutionCount;
                executionCountScore = (1 - executionCountRatio) * 0.2;
            }
        }
        // 可用性占比15%（基于限流器的可用性评估）
        double availableRateScore = calculateAvailableRate(source) * 0.15;

        return weightScore + successRateScore + executionCountScore + availableRateScore;
    }

    /**
     * 标准化权重值，将其映射到0-1范围内
     *
     * @param weight 权重值
     * @return 标准化后的权重值 (0.0 - 1.0)
     */
    private double normalizeWeight(int weight) {
        // 假设权重范围在0-100之间，可以根据实际情况调整
        return Math.min(1.0, Math.max(0.0, (double) weight / 100.0));
    }

    /**
     * 计算数据源的可用性比率
     * 通过评估限流器的状态和平均响应时间来计算数据源的可用性
     *
     * @param source 数据源
     *
     * @return 可用性比率 (0.0 - 1.0)
     */
    private double calculateAvailableRate(IpSource source) {
        // 检查数据源是否可用
        if (!source.isAvailable()) {
            return 0.0;
        }

        // 对于 AbstractNetworkIpSource 类型的数据源，我们可以进一步评估其限流器状态和响应时间
        if (source instanceof AbstractNetworkIpSource abstractNetworkIpSource) {
            // 检查最近一次获取令牌的时间，如果超过一定时间未使用，则认为可用性较高
            long timeSinceLastAcquire = System.currentTimeMillis() - abstractNetworkIpSource.getLastAcquireTime();

            // 如果最近5秒内没有请求，则认为系统负载较低
            if (timeSinceLastAcquire > 5000) {
                return 1.0;
            }

            // 综合考虑限流等待时间和平均响应时间
            double lastWaitTime = abstractNetworkIpSource.getLastAcquireWaitTime();
            double averageResponseTime = abstractNetworkIpSource.getAverageResponseTime();

            // 计算基于限流等待时间的可用性评分
            double waitTimeScore = calculateWaitTimeScore(lastWaitTime);

            // 计算基于平均响应时间的可用性评分
            double responseTimeScore = calculateResponseTimeScore(averageResponseTime);

            // 综合两个评分，其中限流等待时间占60%，平均响应时间占40%
            return waitTimeScore * 0.6 + responseTimeScore * 0.4;
        }

        // 对于其他类型的源，基于可用性返回默认值
        return 0.9;
    }

    /**
     * 根据限流等待时间计算可用性评分
     *
     * @param waitTime 等待时间（秒）
     * @return 可用性评分 (0.0 - 1.0)
     */
    private double calculateWaitTimeScore(double waitTime) {
        // 如果等待时间小于10毫秒，认为系统负载很低
        if (waitTime < 0.01) {
            return 0.9;
        }

        // 如果等待时间在10毫秒到100毫秒之间，认为系统负载中等
        if (waitTime < 0.1) {
            return 0.7;
        }

        // 如果等待时间在100毫秒到500毫秒之间，认为系统负载较高
        if (waitTime < 0.5) {
            return 0.5;
        }

        // 如果等待时间超过500毫秒，认为系统负载很高
        return 0.3;
    }

    /**
     * 根据平均响应时间计算可用性评分
     *
     * @param responseTime 平均响应时间（毫秒）
     * @return 可用性评分 (0.0 - 1.0)
     */
    private double calculateResponseTimeScore(double responseTime) {
        // 如果平均响应时间小于50毫秒，认为系统响应很快
        if (responseTime < 50) {
            return 1.0;
        }

        // 如果平均响应时间在50毫秒到200毫秒之间，认为系统响应正常
        if (responseTime < 200) {
            return 0.8;
        }

        // 如果平均响应时间在200毫秒到500毫秒之间，认为系统响应较慢
        if (responseTime < 500) {
            return 0.6;
        }

        // 如果平均响应时间在500毫秒到1000毫秒之间，认为系统响应很慢
        if (responseTime < 1000) {
            return 0.4;
        }

        // 如果平均响应时间超过1000毫秒，认为系统响应极慢
        return 0.2;
    }
}
