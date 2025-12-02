package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import org.lionsoul.ip2region.Config;
import org.lionsoul.ip2region.Ip2Region;
import org.lionsoul.ip2region.xdb.XdbException;

import java.io.IOException;

/**
 * 本地IP解析器，基于ip2region数据库实现。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class LocalIp2RegionResolver extends AbstractIpSource {

    private final Ip2Region searcher;


    /**
     * 构造函数
     *
     * @param v4DbFile      ip2region数据库ipv4文件路径
     * @param v6DbFile      ip2region数据库ipv6文件路径
     * @param isVectorIndex 是否使用向量索引
     * @param isBuffer      是否使用缓存
     * @param name          解析器名称
     * @param weight        解析器权重
     *
     * @throws IOException IO异常
     */
    public LocalIp2RegionResolver(String v4DbFile, String v6DbFile, boolean isVectorIndex, boolean isBuffer, String name, int weight) throws IOException, XdbException {
        super(name, weight);
        Config ipv4Config;
        Config ipv6Config = null;
        if (isBuffer) {
            ipv4Config = Config.custom().setCachePolicy(Config.BufferCache).setXdbPath(v4DbFile).setSeachers(15).asV4();
            if (v6DbFile != null && !v6DbFile.isEmpty()) {
                ipv6Config = Config.custom().setCachePolicy(Config.BufferCache).setXdbPath(v6DbFile).setSeachers(15).asV6();
            }
        } else if (isVectorIndex) {
            ipv4Config = Config.custom().setCachePolicy(Config.VIndexCache).setXdbPath(v4DbFile).setSeachers(15).asV4();
            if (v6DbFile != null && !v6DbFile.isEmpty()) {
                ipv6Config = Config.custom().setCachePolicy(Config.VIndexCache).setXdbPath(v6DbFile).setSeachers(15).asV6();
            }
        } else {
            ipv4Config = Config.custom().setCachePolicy(Config.NoCache).setXdbPath(v4DbFile).setSeachers(15).asV4();
            if (v6DbFile != null && !v6DbFile.isEmpty()) {
                ipv6Config = Config.custom().setCachePolicy(Config.NoCache).setXdbPath(v6DbFile).setSeachers(15).asV6();
            }
        }
        this.searcher = Ip2Region.create(ipv4Config, ipv6Config);

    }


    /**
     * 构造函数
     *
     * @param searcher ip2region搜索引擎
     * @param name     解析器名称
     * @param weight   解析器权重
     */
    public LocalIp2RegionResolver(Ip2Region searcher, String name, int weight) {
        super(name, weight);
        this.searcher = searcher;
    }

    @Override
    public IpInfo query(String ip) throws Exception {

        try {
            String region = searcher.search(ip);
            updateSuccessStats();
            return IpInfo.fromString(ip, region);
        } catch (Exception e) {
            updateFailureStats();
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        // 本地数据源始终可用
        return true;
    }

    /**
     * 关闭资源
     *
     * @throws IOException IO异常
     */
    public void close() throws IOException, InterruptedException {
        if (searcher != null) {
            searcher.close();
        }
    }
}
