package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import cn.hehouhui.ip2region.resolver.*;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * IP查询引擎工厂类，提供多种创建IpQueryEngine实例的方法。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpQueryEngineFactory {

    /**
     * 创建只包含本地ip2region数据源的查询引擎
     *
     * @param dbPath           ip2region数据库文件路径
     * @param permitsPerSecond 限流速率（每秒请求数）
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithLocalSource(String dbPath, double permitsPerSecond) throws IOException {
        Searcher searcher = Searcher.newWithBuffer(Searcher.loadContentFromFile(dbPath));
        LocalIp2RegionResolver localSource = new LocalIp2RegionResolver(searcher, permitsPerSecond, "LocalIp2Region", 100);

        List<IpSource> sources = new ArrayList<>();
        sources.add(localSource);

        return new IpQueryEngine(sources);
    }

    /**
     * 创建包含多个免费API数据源的查询引擎
     *
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createWithFreeApiSources(double taobaoPermitsPerSecond, double ipApiCoPermitsPerSecond) {
        return createWithFreeApiSources(taobaoPermitsPerSecond, ipApiCoPermitsPerSecond, null);
    }

    /**
     * 创建包含多个免费API数据源的查询引擎
     *
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param httpRequestHandler      HTTP请求处理器
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createWithFreeApiSources(double taobaoPermitsPerSecond, double ipApiCoPermitsPerSecond,
                                                         HttpRequestHandler httpRequestHandler) {
        TaobaoIpResolver taobaoSource = httpRequestHandler == null ?
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90) :
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90, httpRequestHandler);

        IpApiCoResolver ipApiCoSource = httpRequestHandler == null ?
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80) :
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80, httpRequestHandler);

        List<IpSource> sources = new ArrayList<>();
        sources.add(taobaoSource);
        sources.add(ipApiCoSource);

        return new IpQueryEngine(sources);
    }

    /**
     * 创建包含所有免费API数据源的查询引擎
     *
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param pacificPermitsPerSecond Pacific网络API限流速率
     * @param ip9PermitsPerSecond     IP9 API限流速率
     * @param ipInfoPermitsPerSecond  IPInfo API限流速率
     * @param xxlbPermitsPerSecond    XXLB API限流速率
     * @param vorePermitsPerSecond    Vore API限流速率
     * @param ipMoePermitsPerSecond   IP-MOE API限流速率
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createWithAllFreeApiSources(
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        double pacificPermitsPerSecond,
        double ip9PermitsPerSecond,
        double ipInfoPermitsPerSecond,
        double xxlbPermitsPerSecond,
        double vorePermitsPerSecond,
        double ipMoePermitsPerSecond) {
        return createWithAllFreeApiSources(taobaoPermitsPerSecond, ipApiCoPermitsPerSecond, pacificPermitsPerSecond,
            ip9PermitsPerSecond, ipInfoPermitsPerSecond, xxlbPermitsPerSecond, vorePermitsPerSecond, ipMoePermitsPerSecond, null);
    }

    /**
     * 创建包含所有免费API数据源的查询引擎
     *
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param pacificPermitsPerSecond Pacific网络API限流速率
     * @param ip9PermitsPerSecond     IP9 API限流速率
     * @param ipInfoPermitsPerSecond  IPInfo API限流速率
     * @param xxlbPermitsPerSecond    XXLB API限流速率
     * @param vorePermitsPerSecond    Vore API限流速率
     * @param ipMoePermitsPerSecond   IP-MOE API限流速率
     * @param httpRequestHandler      HTTP请求处理器
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createWithAllFreeApiSources(
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        double pacificPermitsPerSecond,
        double ip9PermitsPerSecond,
        double ipInfoPermitsPerSecond,
        double xxlbPermitsPerSecond,
        double vorePermitsPerSecond,
        double ipMoePermitsPerSecond,
        HttpRequestHandler httpRequestHandler) {

        TaobaoIpResolver taobaoSource = httpRequestHandler == null ?
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90) :
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90, httpRequestHandler);

        IpApiCoResolver ipApiCoSource = httpRequestHandler == null ?
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80) :
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80, httpRequestHandler);

        PacificIpResolver pacificSource = httpRequestHandler == null ?
            new PacificIpResolver(pacificPermitsPerSecond, "Pacific", 85) :
            new PacificIpResolver(pacificPermitsPerSecond, "Pacific", 85, httpRequestHandler);

        Ip9Resolver ip9Source = httpRequestHandler == null ?
            new Ip9Resolver(ip9PermitsPerSecond, "IP9", 75) :
            new Ip9Resolver(ip9PermitsPerSecond, "IP9", 75, httpRequestHandler);

        IpInfoResolver ipInfoSource = httpRequestHandler == null ?
            new IpInfoResolver(ipInfoPermitsPerSecond, "IPInfo", 70) :
            new IpInfoResolver(ipInfoPermitsPerSecond, "IPInfo", 70, httpRequestHandler);

        XxlbResolver xxlbSource = httpRequestHandler == null ?
            new XxlbResolver(xxlbPermitsPerSecond, "XXLB", 70) :
            new XxlbResolver(xxlbPermitsPerSecond, "XXLB", 70, httpRequestHandler);

        VoreResolver voreSource = httpRequestHandler == null ?
            new VoreResolver("Vore", 75, vorePermitsPerSecond, null) :
            new VoreResolver("Vore", 75, vorePermitsPerSecond, httpRequestHandler);

        IpMoeResolver ipMoeSource = httpRequestHandler == null ?
            new IpMoeResolver("IP-MOE", 75, ipMoePermitsPerSecond, null) :
            new IpMoeResolver("IP-MOE", 75, ipMoePermitsPerSecond, httpRequestHandler);

        List<IpSource> sources = new ArrayList<>();
        sources.add(taobaoSource);
        sources.add(ipApiCoSource);
        sources.add(pacificSource);
        sources.add(ip9Source);
        sources.add(ipInfoSource);
        sources.add(xxlbSource);
        sources.add(voreSource);
        sources.add(ipMoeSource);

        return new IpQueryEngine(sources);
    }

    /**
     * 创建混合数据源的查询引擎（本地+免费API）
     *
     * @param dbPath                  ip2region数据库文件路径
     * @param localPermitsPerSecond   本地数据源限流速率
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithMixedSources(
        String dbPath,
        double localPermitsPerSecond,
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond) throws IOException {
        return createWithMixedSources(dbPath, localPermitsPerSecond, taobaoPermitsPerSecond, ipApiCoPermitsPerSecond, null);
    }

    /**
     * 创建混合数据源的查询引擎（本地+免费API）
     *
     * @param dbPath                  ip2region数据库文件路径
     * @param localPermitsPerSecond   本地数据源限流速率
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param httpRequestHandler      HTTP请求处理器
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithMixedSources(
        String dbPath,
        double localPermitsPerSecond,
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        HttpRequestHandler httpRequestHandler) throws IOException {

        // 创建本地数据源
        Searcher searcher = Searcher.newWithBuffer(Searcher.loadContentFromFile(dbPath));
        LocalIp2RegionResolver localSource = new LocalIp2RegionResolver(searcher, localPermitsPerSecond, "LocalIp2Region", 100);

        // 创建API数据源
        TaobaoIpResolver taobaoSource = httpRequestHandler == null ?
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90) :
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90, httpRequestHandler);

        IpApiCoResolver ipApiCoSource = httpRequestHandler == null ?
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80) :
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80, httpRequestHandler);

        List<IpSource> sources = new ArrayList<>();
        sources.add(localSource);
        sources.add(taobaoSource);
        sources.add(ipApiCoSource);

        return new IpQueryEngine(sources);
    }

    /**
     * 创建完整的混合数据源查询引擎（本地+所有免费API）
     *
     * @param ip2regionDbPath         ip2region数据库文件路径
     * @param localPermitsPerSecond   本地ip2region数据源限流速率
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param pacificPermitsPerSecond Pacific网络API限流速率
     * @param ip9PermitsPerSecond     IP9 API限流速率
     * @param ipInfoPermitsPerSecond  IPInfo API限流速率
     * @param xxlbPermitsPerSecond    XXLB API限流速率
     * @param vorePermitsPerSecond    Vore API限流速率
     * @param ipMoePermitsPerSecond   IP-MOE API限流速率
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithAllSources(
        String ip2regionDbPath,
        double localPermitsPerSecond,
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        double pacificPermitsPerSecond,
        double ip9PermitsPerSecond,
        double ipInfoPermitsPerSecond,
        double xxlbPermitsPerSecond,
        double vorePermitsPerSecond,
        double ipMoePermitsPerSecond) throws IOException {
        return createWithAllSources(ip2regionDbPath, localPermitsPerSecond, taobaoPermitsPerSecond, ipApiCoPermitsPerSecond,
            pacificPermitsPerSecond, ip9PermitsPerSecond, ipInfoPermitsPerSecond, xxlbPermitsPerSecond, vorePermitsPerSecond, ipMoePermitsPerSecond, null);
    }

    /**
     * 创建完整的混合数据源查询引擎（本地+所有免费API）
     *
     * @param ip2regionDbPath         ip2region数据库文件路径
     * @param localPermitsPerSecond   本地ip2region数据源限流速率
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     * @param pacificPermitsPerSecond Pacific网络API限流速率
     * @param ip9PermitsPerSecond     IP9 API限流速率
     * @param ipInfoPermitsPerSecond  IPInfo API限流速率
     * @param xxlbPermitsPerSecond    XXLB API限流速率
     * @param vorePermitsPerSecond    Vore API限流速率
     * @param ipMoePermitsPerSecond   IP-MOE API限流速率
     * @param httpRequestHandler      HTTP请求处理器
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithAllSources(
        String ip2regionDbPath,
        double localPermitsPerSecond,
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        double pacificPermitsPerSecond,
        double ip9PermitsPerSecond,
        double ipInfoPermitsPerSecond,
        double xxlbPermitsPerSecond,
        double vorePermitsPerSecond,
        double ipMoePermitsPerSecond,
        HttpRequestHandler httpRequestHandler) throws IOException {

        // 创建本地数据源
        Searcher ip2regionSearcher = Searcher.newWithBuffer(Searcher.loadContentFromFile(ip2regionDbPath));
        LocalIp2RegionResolver localSource = new LocalIp2RegionResolver(ip2regionSearcher, localPermitsPerSecond, "LocalIp2Region", 100);

        // 创建API数据源
        TaobaoIpResolver taobaoSource = httpRequestHandler == null ?
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90) :
            new TaobaoIpResolver(taobaoPermitsPerSecond, "TaobaoAPI", 90, httpRequestHandler);

        IpApiCoResolver ipApiCoSource = httpRequestHandler == null ?
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80) :
            new IpApiCoResolver(ipApiCoPermitsPerSecond, "IpApiCo", 80, httpRequestHandler);

        PacificIpResolver pacificSource = httpRequestHandler == null ?
            new PacificIpResolver(pacificPermitsPerSecond, "Pacific", 85) :
            new PacificIpResolver(pacificPermitsPerSecond, "Pacific", 85, httpRequestHandler);

        Ip9Resolver ip9Source = httpRequestHandler == null ?
            new Ip9Resolver(ip9PermitsPerSecond, "IP9", 75) :
            new Ip9Resolver(ip9PermitsPerSecond, "IP9", 75, httpRequestHandler);

        IpInfoResolver ipInfoSource = httpRequestHandler == null ?
            new IpInfoResolver(ipInfoPermitsPerSecond, "IPInfo", 70) :
            new IpInfoResolver(ipInfoPermitsPerSecond, "IPInfo", 70, httpRequestHandler);

        XxlbResolver xxlbSource = httpRequestHandler == null ?
            new XxlbResolver(xxlbPermitsPerSecond, "XXLB", 70) :
            new XxlbResolver(xxlbPermitsPerSecond, "XXLB", 70, httpRequestHandler);

        VoreResolver voreSource = httpRequestHandler == null ?
            new VoreResolver("Vore", 75, 10, null) :
            new VoreResolver("Vore", 75, 10, httpRequestHandler);

        IpMoeResolver ipMoeSource = httpRequestHandler == null ?
            new IpMoeResolver("IP-MOE", 75, 10, null) :
            new IpMoeResolver("IP-MOE", 75, 10, httpRequestHandler);

        List<IpSource> sources = new ArrayList<>();
        sources.add(localSource);
        sources.add(taobaoSource);
        sources.add(ipApiCoSource);
        sources.add(pacificSource);
        sources.add(ip9Source);
        sources.add(ipInfoSource);
        sources.add(xxlbSource);
        sources.add(voreSource);
        sources.add(ipMoeSource);

        return new IpQueryEngine(sources);
    }

    /**
     * 创建自定义数据源的查询引擎
     *
     * @param sources 数据源列表
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createWithCustomSources(List<IpSource> sources) {
        return new IpQueryEngine(sources);
    }
}
