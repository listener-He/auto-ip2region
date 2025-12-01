package cn.hehouhui.ip2region.core;

import cn.hehouhui.ip2region.IpInfo;

/**
 * IP数据源接口，代表一个具体的IP查询实现。
 * 可以是本地数据库实现，也可以是远程API实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public interface IpSource {

    /**
     * 查询IP信息
     *
     * @param ip IP地址
     * @return IP信息
     * @throws Exception 查询异常
     */
    IpInfo query(String ip) throws Exception;

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */
    String getName();

    /**
     * 获取数据源权重（数值越高优先级越高）
     *
     * @return 权重值
     */
    int getWeight();

    /**
     * 获取成功率（0.0-1.0之间）
     *
     * @return 成功率
     */
    double getSuccessRate();

    /**
     * 获取总执行次数
     *
     * @return 执行次数
     */
    long getExecutionCount();

    /**
     * 获取失败次数
     *
     * @return 失败次数
     */
    long getFailureCount();

    /**
     * 检查数据源是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}