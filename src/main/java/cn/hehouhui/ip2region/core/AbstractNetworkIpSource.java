package cn.hehouhui.ip2region.core;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import com.google.common.util.concurrent.RateLimiter;

import java.io.IOException;
import java.util.Optional;

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
     * 更新限流器获取时间信息
     *
     * @param waitTime 等待时间（秒）
     */
    protected void updateAcquireTimeStats(double waitTime) {
        lastAcquireTime = System.currentTimeMillis();
        lastAcquireWaitTime = waitTime;
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
        try {
            Optional<IpInfo> ipInfo = request(ip);
            if (ipInfo.isPresent()) {
                updateSuccessStats();
                return ipInfo.get();
            } else {
                updateFailureStats();
                return IpInfo.fromString(ip, "unknown");
            }
        } catch (IOException | InterruptedException e) {
            updateFailureStats();
            throw new Exception("Network error occurred", e);
        } catch (Exception e) {
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
