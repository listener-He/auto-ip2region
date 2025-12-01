package cn.hehouhui.ip2region.loadbalancer;

import cn.hehouhui.ip2region.core.IpSource;

import java.util.List;

/**
 * 负载均衡器接口，定义了选择最佳数据源的策略。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public interface LoadBalancer {

    /**
     * 根据负载均衡策略选择最佳的数据源
     *
     * @param sources 可用的数据源列表
     * @return 选中的数据源
     */
    IpSource select(List<IpSource> sources);
}