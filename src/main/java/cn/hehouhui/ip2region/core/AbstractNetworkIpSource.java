package cn.hehouhui.ip2region.core;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 抽象的通过网络请求解析ip
 *
 * @author HeHui
 * @date 2025-12-01 22:59
 */
public abstract class AbstractNetworkIpSource extends AbstractIpSource {

    protected final RateLimiter rateLimiter;

    // 记录最近一次限流等待时间
    protected volatile long lastAcquireTime = 0;

    // 记录最近一次限流等待时间
    protected volatile double lastAcquireWaitTime = 0;
    
    // 记录总响应时间（毫秒）
    protected final AtomicLong totalResponseTime = new AtomicLong(0);
    
    // 记录响应次数
    protected final AtomicLong responseCount = new AtomicLong(0);

    protected final HttpRequestHandler httpRequestHandler;

    /**
     * 构造函数
     *
     * @param name                解析器名称
     * @param weight              解析器权重
     * @param permitsPerSecond    每秒许可数（限流速率）
     * @param httpRequestHandler  HTTP请求处理器
     */
    public AbstractNetworkIpSource(String name, int weight, double permitsPerSecond, HttpRequestHandler httpRequestHandler) {
        super(name, weight);
        this.httpRequestHandler = httpRequestHandler;
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
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
     * 获取平均响应时间（毫秒）
     *
     * @return 平均响应时间（毫秒）
     */
    public double getAverageResponseTime() {
        long count = responseCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) totalResponseTime.get() / count;
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
    
    /**
     * 更新响应时间统计信息
     *
     * @param responseTime 响应时间（毫秒）
     */
    protected void updateResponseTimeStats(long responseTime) {
        totalResponseTime.addAndGet(responseTime);
        responseCount.incrementAndGet();
    }


    /**
     * 查询IP信息
     *
     * @param ip IP地址
     *
     * @return IP信息
     *
     * @throws Exception 查询异常
     */
    @Override
    public IpInfo query(String ip) throws Exception {
        if (ip == null || ip.isEmpty()) {
            return IpInfo.fromString(ip, "unknown");
        }
        double waitTime = rateLimiter.acquire();
        updateAcquireTimeStats(waitTime);
        
        long startTime = System.currentTimeMillis();
        try {
            Optional<IpInfo> ipInfo = request(ip);
            long responseTime = System.currentTimeMillis() - startTime;
            updateResponseTimeStats(responseTime);
            
            if (ipInfo.isPresent()) {
                updateSuccessStats();
                return ipInfo.get();
            } else {
                updateFailureStats();
                return IpInfo.fromString(ip, "unknown");
            }
        } catch (IOException | InterruptedException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            updateResponseTimeStats(responseTime);
            updateFailureStats();
            throw new Exception("Network error occurred", e);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            updateResponseTimeStats(responseTime);
            updateFailureStats();
            throw e;
        }
    }



    /**
     * 发送请求并解析IP信息
     *
     * @param ip IP地址
     *
     * @return IP信息
     *
     * @throws Exception 请求异常
     */
    protected abstract Optional<IpInfo> request(String ip) throws Exception;
}