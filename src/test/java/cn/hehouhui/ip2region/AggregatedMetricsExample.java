package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.resolver.LocalIp2RegionResolver;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;
import java.util.List;

/**
 * 聚合指标使用示例
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class AggregatedMetricsExample {

    public static void main(String[] args) throws IOException {
        // 创建本地数据源
        Searcher searcher = Searcher.newWithBuffer(Searcher.loadContentFromFile("path/to/ip2region.xdb"));
        IpSource localSource = new LocalIp2RegionResolver(searcher,  "LocalIp2Region", 100);

        // 创建查询引擎
        IpQueryEngine engine = new IpQueryEngine(List.of(localSource));

        // 执行一些查询
        try {
            engine.query("8.8.8.8");
            engine.query("114.114.114.114");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取聚合指标
        AggregatedMetrics metrics = engine.getAggregatedMetrics();

        // 打印指标
        System.out.println("=== 聚合指标 ===");
        System.out.println("缓存大小: " + metrics.getCacheSize());
        System.out.println("缓存统计: " + metrics.getCacheStats());

        System.out.println("\n=== 本地数据源指标 ===");
        AggregatedMetrics.DataSourceMetrics localMetrics = metrics.getLocalMetrics();
        System.out.println("执行次数: " + localMetrics.getExecutionCount());
        System.out.println("失败次数: " + localMetrics.getFailureCount());
        System.out.println("成功率: " + String.format("%.2f%%", localMetrics.getSuccessRate() * 100));

        System.out.println("\n=== 网络数据源指标 ===");
        AggregatedMetrics.DataSourceMetrics networkMetrics = metrics.getNetworkMetrics();
        System.out.println("执行次数: " + networkMetrics.getExecutionCount());
        System.out.println("失败次数: " + networkMetrics.getFailureCount());
        System.out.println("成功率: " + String.format("%.2f%%", networkMetrics.getSuccessRate() * 100));
        System.out.println("平均响应时间: " + String.format("%.2f ms", networkMetrics.getAverageResponseTime()));

        System.out.println("\n=== 总体指标 ===");
        AggregatedMetrics.DataSourceMetrics totalMetrics = metrics.getTotalMetrics();
        System.out.println("执行次数: " + totalMetrics.getExecutionCount());
        System.out.println("失败次数: " + totalMetrics.getFailureCount());
        System.out.println("成功率: " + String.format("%.2f%%", totalMetrics.getSuccessRate() * 100));
    }
}
