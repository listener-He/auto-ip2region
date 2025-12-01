package cn.hehouhui.ip2region.core;

import cn.hehouhui.ip2region.http.HttpRequestHandler;

/**
 * 抽象的通过网络请求解析ip
 *
 * @author HeHui
 * @date 2025-12-01 22:59
 */
public abstract class AbstractNetworkIpSource extends AbstractIpSource {

    protected final HttpRequestHandler httpRequestHandler;
    public AbstractNetworkIpSource(String name, int weight, double permitsPerSecond, HttpRequestHandler httpRequestHandler) {
        super(name, weight, permitsPerSecond);
        this.httpRequestHandler = httpRequestHandler;
    }
}
