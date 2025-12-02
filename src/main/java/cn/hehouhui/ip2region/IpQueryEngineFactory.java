package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import cn.hehouhui.ip2region.resolver.*;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.File;
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
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithLocalSource(String dbPath) throws IOException {
        try {
            Class.forName("org.lionsoul.ip2region.xdb.Searcher");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("缺少ip2region依赖，请添加相关依赖后再使用此功能: org.lionsoul:ip2region", e);
        }

        try {
            LocalIp2RegionResolver localSource = new LocalIp2RegionResolver(dbPath, false, false, "LocalIp2Region", 100);
            List<IpSource> sources = new ArrayList<>();
            sources.add(localSource);

            return new IpQueryEngine(sources);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("创建ip2region数据源时出错", e);
        }
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
     * @param taobaoPermitsPerSecond  淘宝API限流速率
     * @param ipApiCoPermitsPerSecond ipapi.co限流速率
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithMixedSources(
        String dbPath,
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond) throws IOException {
        return createWithMixedSources(dbPath, taobaoPermitsPerSecond, ipApiCoPermitsPerSecond, null);
    }

    /**
     * 创建混合数据源的查询引擎（本地+免费API）
     *
     * @param dbPath                  ip2region数据库文件路径
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
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        HttpRequestHandler httpRequestHandler) throws IOException {

        // 创建本地数据源
        LocalIp2RegionResolver localSource;
        try {
            Class.forName("org.lionsoul.ip2region.xdb.Searcher");
            localSource = new LocalIp2RegionResolver(dbPath, false, false, "LocalIp2Region", 100);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("缺少ip2region依赖，请添加相关依赖后再使用此功能: org.lionsoul:ip2region", e);
        } catch (Exception e) {
            throw new IOException("创建ip2region数据源时出错", e);
        }

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
        double taobaoPermitsPerSecond,
        double ipApiCoPermitsPerSecond,
        double pacificPermitsPerSecond,
        double ip9PermitsPerSecond,
        double ipInfoPermitsPerSecond,
        double xxlbPermitsPerSecond,
        double vorePermitsPerSecond,
        double ipMoePermitsPerSecond) throws IOException {
        return createWithAllSources(ip2regionDbPath, taobaoPermitsPerSecond, ipApiCoPermitsPerSecond,
            pacificPermitsPerSecond, ip9PermitsPerSecond, ipInfoPermitsPerSecond, xxlbPermitsPerSecond, vorePermitsPerSecond, ipMoePermitsPerSecond, null);
    }

    /**
     * 创建完整的混合数据源查询引擎（本地+所有免费API）
     *
     * @param ip2regionDbPath         ip2region数据库文件路径
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
        org.lionsoul.ip2region.xdb.Searcher ip2regionSearcher;
        LocalIp2RegionResolver localSource;
        try {
            Class.forName("org.lionsoul.ip2region.xdb.Searcher");
            ip2regionSearcher = org.lionsoul.ip2region.xdb.Searcher.newWithBuffer(
                    org.lionsoul.ip2region.xdb.Searcher.loadContentFromFile(ip2regionDbPath));
            localSource = new LocalIp2RegionResolver(ip2regionSearcher, "LocalIp2Region", 100);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw new RuntimeException("缺少ip2region依赖，请添加相关依赖后再使用此功能", e);
        }

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
            new VoreResolver("Vore", 75, vorePermitsPerSecond, null) :
            new VoreResolver("Vore", 75, vorePermitsPerSecond, httpRequestHandler);

        IpMoeResolver ipMoeSource = httpRequestHandler == null ?
            new IpMoeResolver("IP-MOE", 75, ipMoePermitsPerSecond, null) :
            new IpMoeResolver("IP-MOE", 75, ipMoePermitsPerSecond, httpRequestHandler);

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
     * 创建包含GeoIP2本地数据源的查询引擎
     *
     * @param dbFile           GeoIP2数据库文件
     * @param permitsPerSecond 限流速率（每秒请求数）
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithGeoIP2Source(File dbFile, double permitsPerSecond) throws IOException {
        try {
            Class.forName("com.maxmind.geoip2.DatabaseReader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("缺少GeoIP2依赖，请添加相关依赖后再使用此功能", e);
        }

        try {
            com.maxmind.geoip2.DatabaseReader reader = new com.maxmind.geoip2.DatabaseReader.Builder(dbFile).build();
            GeoIP2Resolver geoIP2Source = new GeoIP2Resolver(reader, "GeoIP2", 100);
            List<IpSource> sources = new ArrayList<>();
            sources.add(geoIP2Source);
            return new IpQueryEngine(sources);
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("缺少GeoIP2依赖，请添加相关依赖后再使用此功能", e);
        }
    }

    /**
     * 创建包含GeoIP2本地数据源的查询引擎
     *
     * @param reader           GeoIP2数据库读取器
     * @param permitsPerSecond 限流速率（每秒请求数）
     *
     * @return IP查询引擎
     *
     * @throws IOException 数据库文件读取异常
     */
    public static IpQueryEngine createWithGeoIP2Source(com.maxmind.geoip2.DatabaseReader reader, double permitsPerSecond) throws IOException {
        try {
            Class.forName("com.maxmind.geoip2.DatabaseReader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("缺少GeoIP2依赖，请添加相关依赖后再使用此功能", e);
        }

        try {
            GeoIP2Resolver geoIP2Source = new GeoIP2Resolver(reader, "GeoIP2", 100);
            List<IpSource> sources = new ArrayList<>();
            sources.add(geoIP2Source);
            return new IpQueryEngine(sources);
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("缺少GeoIP2依赖，请添加相关依赖后再使用此功能", e);
        }
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
