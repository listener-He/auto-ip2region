package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.fallback.LocalFirstFallbackStrategy;
import cn.hehouhui.ip2region.http.DefaultHttpRequestHandler;
import cn.hehouhui.ip2region.http.HttpRequestHandler;
import cn.hehouhui.ip2region.loadbalancer.WeightedLoadBalancer;
import cn.hehouhui.ip2region.resolver.*;
import com.maxmind.db.Reader;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * IP查询引擎工厂类，提供多种创建IpQueryEngine实例的方法。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpQueryEngineFactory {

    private static volatile Boolean LOAD_GEO_IP_CLASS;

    private static volatile Boolean LOAD_IP2REGION_CLASS;


    public static final String GEO_IP_DB_FILE = "GeoLite2-City.mmdb";

    public static final String IP2REGION_DB_V4_FILE = "ip2region_v4.xdb";

    public static final String IP2REGION_DB_V6_FILE = "ip2region_v6.xdb";

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.pathSeparator + "auto-ip2region";

    public static final String RESOURCES_DIR = "auto-ip2region";

    /**
     * 尝试从资源目录加载GeoIP2数据源
     * 该方法会检查classpath中是否存在GeoIP2依赖，并尝试从资源目录加载GeoLite2-City.mmdb数据库文件
     * 支持从jar包内和文件系统两种方式加载数据库文件
     *
     * @return 包含GeoIP2解析器的Optional，如果加载失败或缺少依赖则返回empty
     */
    public static Optional<IpSource> tryLoadGeoIpSource() {
        if (LOAD_GEO_IP_CLASS == null) {
            synchronized (GeoIP2Resolver.class) {
                if (LOAD_GEO_IP_CLASS == null) {
                    try {
                        Class.forName("com.maxmind.geoip2.DatabaseReader");
                        LOAD_GEO_IP_CLASS = true;
                    } catch (ClassNotFoundException e) {
                        LOAD_GEO_IP_CLASS = false;
                    }
                }
            }
        }
        if (!LOAD_GEO_IP_CLASS) {
            return Optional.empty();
        }
        // 尝试从资源目录加载GeoIP2数据库文件
        try {
            // 首先尝试从项目的resources目录加载
            java.net.URL resourceUrl = IpQueryEngineFactory.class.getClassLoader()
                .getResource(RESOURCES_DIR + File.pathSeparator + GEO_IP_DB_FILE);
            if (resourceUrl == null) {
                return Optional.empty();
            }
            // 如果在jar包内找到了文件
            if ("jar".equals(resourceUrl.getProtocol())) {
                // 从jar包中提取文件到临时目录
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory(TEMP_DIR + File.pathSeparator + "geoip2");
                java.nio.file.Path tempFile = tempDir.resolve(GEO_IP_DB_FILE);
                try (java.io.InputStream is = resourceUrl.openStream()) {
                    java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                // 创建GeoIP2解析器
                com.maxmind.geoip2.DatabaseReader reader = new com.maxmind.geoip2.DatabaseReader.Builder(tempFile.toFile())
                    .fileMode(Reader.FileMode.MEMORY_MAPPED)
                    .build();
                GeoIP2Resolver resolver = new GeoIP2Resolver(reader, "GeoIP2-Resource", 95);
                return Optional.of(resolver);
            }
            // 如果是文件系统中的文件
            else if ("file".equals(resourceUrl.getProtocol())) {
                java.io.File dbFile = new java.io.File(resourceUrl.toURI());
                com.maxmind.geoip2.DatabaseReader reader = new com.maxmind.geoip2.DatabaseReader.Builder(dbFile)
                    .fileMode(Reader.FileMode.MEMORY_MAPPED).build();
                GeoIP2Resolver resolver = new GeoIP2Resolver(reader, "GeoIP2", 95);
                return Optional.of(resolver);
            }
        } catch (Exception e) {
            // 忽略异常，返回empty
        }

        return Optional.empty();
    }


    /**
     * 尝试从资源目录加载ip2region数据源
     * 该方法会检查classpath中是否存在ip2region依赖，并尝试从资源目录加载IPv4和IPv6的数据库文件
     * 支持从jar包内和文件系统两种方式加载数据库文件
     *
     * @return 包含ip2region解析器的Optional，如果加载失败或缺少依赖则返回empty
     */
    public static Optional<IpSource> tryLoadIp2RegionSource() {
        if (LOAD_IP2REGION_CLASS == null) {
            synchronized (LocalIp2RegionResolver.class) {
                if (LOAD_IP2REGION_CLASS == null) {
                    try {
                        Class.forName("org.lionsoul.ip2region.Ip2Region");
                        LOAD_IP2REGION_CLASS = true;
                    } catch (ClassNotFoundException e) {
                        LOAD_IP2REGION_CLASS = false;
                    }
                }
            }
        }
        if (!LOAD_IP2REGION_CLASS) {
            return Optional.empty();
        }

        // 尝试从资源目录加载ip2region数据库文件
        try {
            // 首先尝试从当前项目的resources目录加载
            java.net.URL v4ResourceUrl = IpQueryEngineFactory.class.getClassLoader()
                .getResource(RESOURCES_DIR + File.pathSeparator + IP2REGION_DB_V4_FILE);
            java.net.URL v6ResourceUrl = IpQueryEngineFactory.class.getClassLoader()
                .getResource(RESOURCES_DIR + File.pathSeparator + IP2REGION_DB_V6_FILE);
            if (v4ResourceUrl == null && v6ResourceUrl == null) {
                return Optional.empty();
            }
            String v4DbFile = null;
            String v6DbFile = null;
            if (v4ResourceUrl != null) {
                // 如果在jar包内找到了文件
                if ("jar".equals(v4ResourceUrl.getProtocol())) {
                    // 从jar包中提取文件到临时目录
                    java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory(TEMP_DIR + File.pathSeparator + "ip2region");
                    java.nio.file.Path tempFile = tempDir.resolve(IP2REGION_DB_V4_FILE);
                    try (java.io.InputStream is = v4ResourceUrl.openStream()) {
                        java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    v4DbFile = tempFile.toString();
                }
                // 如果是文件系统中的文件
                else if ("file".equals(v4ResourceUrl.getProtocol())) {
                    v4DbFile = v4ResourceUrl.toURI().getPath();
                }
            }
            if (v6ResourceUrl != null) {
                // 如果在jar包内找到了文件
                if ("jar".equals(v6ResourceUrl.getProtocol())) {
                    // 从jar包中提取文件到临时目录
                    java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory(TEMP_DIR + File.pathSeparator + "ip2region");
                    java.nio.file.Path tempFile = tempDir.resolve(IP2REGION_DB_V6_FILE);
                    try (java.io.InputStream is = v6ResourceUrl.openStream()) {
                        java.nio.file.Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                } else if ("file".equals(v6ResourceUrl.getProtocol())) {
                    v6DbFile = v6ResourceUrl.toURI().getPath();
                }
            }
            if (v4DbFile != null || v6DbFile != null) {
                LocalIp2RegionResolver resolver = new LocalIp2RegionResolver(v4DbFile, v6DbFile, true, false, "IP2Region", 95);
                return Optional.of(resolver);
            }
        } catch (Exception e) {
            // 忽略异常，返回empty
        }
        return Optional.empty();
    }


    /**
     * 尝试从资源目录加载所有可用的本地数据源
     * 包括GeoIP2和ip2region数据库
     *
     * @return 可用的本地数据源列表
     */
    public static List<IpSource> tryLoadLocalSources() {
        List<IpSource> sources = new ArrayList<>();
        // 尝试加载GeoIP2数据源
        Optional<IpSource> geoIpSource = tryLoadGeoIpSource();
        geoIpSource.ifPresent(sources::add);

        // 尝试加载ip2region数据源
        Optional<IpSource> ip2RegionSource = tryLoadIp2RegionSource();
        ip2RegionSource.ifPresent(sources::add);
        return sources;
    }


    /**
     * 加载免费API数据源列表
     * 根据speedPriority参数决定数据源的权重分配策略
     *
     * @param speedPriority 是否优先速度，true时高权重数据源权重降低以实现更均匀的请求分布
     *
     * @return 免费API数据源列表
     */
    public static List<IpSource> loadFreeApiSources(boolean speedPriority) {
        return loadFreeApiSources(new DefaultHttpRequestHandler(), speedPriority);
    }


    /**
     * 加载免费API数据源列表
     * 根据speedPriority参数决定数据源的权重分配策略
     *
     * @param httpRequestHandler HTTP请求处理器
     * @param speedPriority      是否优先速度，true时高权重数据源权重降低以实现更均匀的请求分布
     *
     * @return 免费API数据源列表
     */
    public static List<IpSource> loadFreeApiSources(HttpRequestHandler httpRequestHandler, boolean speedPriority) {
        TaobaoIpResolver taobaoSource = new TaobaoIpResolver(3, "TaobaoAPI", speedPriority ? 50 : 90, httpRequestHandler);
        IpApiCoResolver ipApiCoSource = new IpApiCoResolver(2, "IpApiCo", speedPriority ? 40 : 70, httpRequestHandler);
        Ip9Resolver ip9Source = new Ip9Resolver(1, "IP9", speedPriority ? 25 : 60, httpRequestHandler);
        IpInfoResolver ipInfoSource = new IpInfoResolver(1, "IPInfo", speedPriority ? 20 : 55, httpRequestHandler);
        XxlbResolver xxlbSource = new XxlbResolver(1, "XXLB", speedPriority ? 15 : 30, httpRequestHandler);
        VoreResolver voreSource = new VoreResolver(1, "Vore", speedPriority ? 10 : 30, httpRequestHandler);
        IpMoeResolver ipMoeSource = new IpMoeResolver(1, "IP-MOE", speedPriority ? 10 : 30, httpRequestHandler);
        PacificIpResolver pacificSource = new PacificIpResolver(1, "Pacific", speedPriority ? 10 : 30, httpRequestHandler);
        ZhengbingdongResolver zhengbingdongSource = new ZhengbingdongResolver(1,"Zhengbingdong", speedPriority ? 10 : 30, httpRequestHandler);
        List<IpSource> sources = new ArrayList<>();
        sources.add(taobaoSource);
        sources.add(ipApiCoSource);
        sources.add(ip9Source);
        sources.add(ipInfoSource);
        sources.add(xxlbSource);
        sources.add(voreSource);
        sources.add(ipMoeSource);
        sources.add(pacificSource);
        sources.add(zhengbingdongSource);
        return sources;
    }


    /**
     * 创建仅包含本地数据源的IP查询引擎
     * 该方法会尝试自动加载所有可用的本地数据源（如GeoIP2和ip2region），并创建相应的查询引擎
     *
     * @param maxCacheSize      最大缓存数量，如果为null则使用默认配置
     * @param expireAfterWrite  缓存项写入后多久过期，如果为null则使用默认配置
     * @param expireAfterAccess 缓存项最后访问后多久过期，如果为null则使用默认配置
     * @return {@link IpQueryEngine } IP查询引擎实例
     * @throws RuntimeException 当没有找到可用的数据源时抛出异常
     */
    public static IpQueryEngine createLocalEngine(Integer maxCacheSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        List<IpSource> sources = tryLoadLocalSources();
        if (sources.isEmpty()) {
            throw new RuntimeException("No available data source found.");
        }
        if (maxCacheSize == null || expireAfterWrite == null || expireAfterAccess == null) {
            return createFromSources(sources);
        }
        return createFromSources(sources, maxCacheSize, expireAfterWrite, expireAfterAccess);
    }

    /**
     * 创建仅包含免费API数据源的IP查询引擎
     * 该方法会加载所有预定义的免费API数据源，并创建相应的查询引擎
     *
     * @param maxCacheSize      最大缓存数量，如果为null则使用默认配置
     * @param expireAfterWrite  缓存项写入后多久过期，如果为null则使用默认配置
     * @param expireAfterAccess 缓存项最后访问后多久过期，如果为null则使用默认配置
     * @return {@link IpQueryEngine } IP查询引擎实例
     * @throws RuntimeException 当没有找到可用的数据源时抛出异常
     */
    public static IpQueryEngine createFreeApiEngine(Integer maxCacheSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        List<IpSource> sources = loadFreeApiSources(false);
        if (sources.isEmpty()) {
            throw new RuntimeException("No available data source found.");
        }
        if (maxCacheSize == null || expireAfterWrite == null || expireAfterAccess == null) {
            return createFromSources(sources);
        }
        return createFromSources(sources, maxCacheSize, expireAfterWrite, expireAfterAccess);
    }

    /**
     * 创建包含所有数据源的IP查询引擎
     * 该方法会加载所有本地数据源和免费API数据源，并创建相应的查询引擎
     *
     * @param speedPriority     是否优先考虑速度，true时会调整API数据源的权重分配
     * @param maxCacheSize      最大缓存数量，如果为null则使用默认配置
     * @param expireAfterWrite  缓存项写入后多久过期，如果为null则使用默认配置
     * @param expireAfterAccess 缓存项最后访问后多久过期，如果为null则使用默认配置
     *
     * @return {@link IpQueryEngine } IP查询引擎实例
     */
    public static IpQueryEngine createAllSourceEngine(boolean speedPriority, Integer maxCacheSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        List<IpSource> sources = tryLoadLocalSources();
        sources.addAll(loadFreeApiSources(speedPriority));
        if (sources.isEmpty()) {
            throw new RuntimeException("No available data source found.");
        }
        if (maxCacheSize == null || expireAfterWrite == null || expireAfterAccess == null) {
            return createFromSources(sources);
        }
        return createFromSources(sources, maxCacheSize, expireAfterWrite, expireAfterAccess);
    }


    /**
     * 创建数据源的查询引擎 (默认策略)
     *
     * @param sources 数据源列表
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createFromSources(List<IpSource> sources) {
        return new IpQueryEngine(sources);
    }


    /**
     * 创建数据源的查询引擎
     *
     * @param sources           数据源列表
     * @param maxCacheSize      最大缓存数量
     * @param expireAfterWrite  缓存项写入后多少秒过期
     * @param expireAfterAccess 缓存项最后访问后多少秒过期
     *
     * @return IP查询引擎
     */
    public static IpQueryEngine createFromSources(List<IpSource> sources, int maxCacheSize, Duration expireAfterWrite, Duration expireAfterAccess) {
        return new IpQueryEngine(sources, new WeightedLoadBalancer(), new LocalFirstFallbackStrategy(), maxCacheSize, expireAfterWrite, expireAfterAccess);
    }
}
