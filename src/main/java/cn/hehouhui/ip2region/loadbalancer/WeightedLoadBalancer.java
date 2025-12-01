package cn.hehouhui.ip2region.loadbalancer;

import cn.hehouhui.ip2region.core.AbstractNetworkIpSource;
import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

        // 如果所有数据源执行次数都为0，则使用随机选择
        if (maxExecutionCount == 0) {
            int index = ThreadLocalRandom.current().nextInt(sources.size());
            return sources.get(index);
        }

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
        double weightScore = source.getWeight() * 0.4;

        // 成功率占比25%
        double successRateScore = source.getSuccessRate() * 0.25;

        // 执行次数占比20%（执行次数越少优先级越高）
        double executionCountRatio = (double) source.getExecutionCount() / maxExecutionCount;
        double executionCountScore = (1 - executionCountRatio) * 0.2;

        // 可用性占比15%（基于限流器的可用性评估）
        double availableRateScore = calculateAvailableRate(source) * 0.15;

        return weightScore + successRateScore + executionCountScore + availableRateScore;
    }

    /**
     * 计算数据源的可用性比率
     * 通过评估限流器的状态来计算数据源的可用性
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

        // 对于 AbstractNetworkIpSource 类型的数据源，我们可以进一步评估其限流器状态
        if (source instanceof AbstractNetworkIpSource abstractIpSource) {

            // 检查最近一次获取令牌的时间，如果超过一定时间未使用，则认为可用性较高
            long timeSinceLastAcquire = System.currentTimeMillis() - abstractIpSource.getLastAcquireTime();

            // 如果最近5秒内没有请求，则认为系统负载较低
            if (timeSinceLastAcquire > 5000) {
                return 1.0;
            }
            // 根据最近一次获取令牌的等待时间评估可用性
            // 等待时间越长，说明系统越繁忙，可用性越低
            double lastWaitTime = abstractIpSource.getLastAcquireWaitTime();

            // 如果等待时间小于10毫秒，认为系统负载很低
            if (lastWaitTime < 0.01) {
                return 0.9;
            }

            // 如果等待时间在10毫秒到100毫秒之间，认为系统负载中等
            if (lastWaitTime < 0.1) {
                return 0.7;
            }

            // 如果等待时间在100毫秒到500毫秒之间，认为系统负载较高
            if (lastWaitTime < 0.5) {
                return 0.5;
            }

            // 如果等待时间超过500毫秒，认为系统负载很高
            return 0.3;
        }

        // 对于其他类型的源，基于可用性返回默认值
        return 0.9;
    }
}
