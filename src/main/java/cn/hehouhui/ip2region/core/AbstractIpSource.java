package cn.hehouhui.ip2region.core;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * IP数据源抽象实现，提供基础的统计和限流功能。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public abstract class AbstractIpSource implements IpSource {
    protected final String name;
    protected final int weight;
    protected final RateLimiter rateLimiter;

    // 统计信息
    protected final AtomicLong executionCount = new AtomicLong(0);
    protected final AtomicLong failureCount = new AtomicLong(0);
    
    // 记录最近一次成功和失败的时间戳
    protected volatile long lastSuccessTime = 0;
    protected volatile long lastFailureTime = 0;
    
    // 记录最近一次限流等待时间
    protected volatile long lastAcquireTime = 0;
    protected volatile double lastAcquireWaitTime = 0;

    /**
     * 构造函数
     *
     * @param name             数据源名称
     * @param weight           数据源权重
     * @param permitsPerSecond 每秒许可数（限流速率）
     */
    public AbstractIpSource(String name, int weight, double permitsPerSecond) {
        this.name = name;
        this.weight = weight;
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public double getSuccessRate() {
        long execCount = executionCount.get();
        if (execCount == 0) {
            return 1.0;
        }
        return (double) (execCount - failureCount.get()) / execCount;
    }

    @Override
    public long getExecutionCount() {
        return executionCount.get();
    }

    @Override
    public long getFailureCount() {
        return failureCount.get();
    }

    @Override
    public boolean isAvailable() {
        // 默认实现：只要成功率不低于50%就算可用
        // 同时考虑最近的执行情况，如果最近执行失败且失败时间在成功时间之后，则暂时不可用
        long execCount = executionCount.get();
        if (execCount == 0) {
            return true; // 尚未执行过，认为可用
        }
        
        // 如果最近一次执行失败，且失败时间在成功时间之后，则暂时不可用
        if (lastFailureTime > lastSuccessTime) {
            // 检查失败后是否经过了一定时间，避免持续失败导致的长时间不可用
            long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime;
            if (timeSinceLastFailure < 5000) { // 5秒内失败则认为暂时不可用
                return false;
            }
        }
        
        // 成功率不低于50%则认为可用
        return getSuccessRate() >= 0.5;
    }
    
    /**
     * 获取最近一次获取令牌的等待时间
     * 
     * @return 等待时间（秒）
     */
    public double getLastAcquireWaitTime() {
        return lastAcquireWaitTime;
    }
    
    /**
     * 获取最近一次获取令牌的时间戳
     * 
     * @return 时间戳（毫秒）
     */
    public long getLastAcquireTime() {
        return lastAcquireTime;
    }
    
    /**
     * 更新成功统计信息
     */
    protected void updateSuccessStats() {
        executionCount.incrementAndGet();
        lastSuccessTime = System.currentTimeMillis();
    }
    
    /**
     * 更新失败统计信息
     */
    protected void updateFailureStats() {
        executionCount.incrementAndGet();
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
    }
    
    /**
     * 更新限流器获取时间信息
     * 
     * @param waitTime 等待时间（秒）
     */
    protected void updateAcquireTimeStats(double waitTime) {
        lastAcquireTime = System.currentTimeMillis();
        lastAcquireWaitTime = waitTime;
    }
}