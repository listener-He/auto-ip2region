package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.resolver.GeoIP2Resolver;
import cn.hehouhui.ip2region.resolver.LocalIp2RegionResolver;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IP查询引擎单元测试类
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpQueryEngineTest {

    @Test
    public void testIpInfoFromString() {
        // Test ip2region format
        IpInfo info = IpInfo.fromString("1.1.1.1", "中国|0|广东省|深圳市|电信");
        assertEquals("1.1.1.1", info.getIp());
        assertEquals("中国", info.getCountry());
        assertEquals("", info.getRegion());
        assertEquals("广东省", info.getProvince());
        assertEquals("深圳市", info.getCity());
        assertEquals("电信", info.getIsp());

        // Test empty case
        IpInfo emptyInfo = IpInfo.fromString("2.2.2.2", "");
        assertEquals("2.2.2.2", emptyInfo.getIp());
        assertEquals("", emptyInfo.getCountry());
        assertEquals("", emptyInfo.getRegion());
        assertEquals("", emptyInfo.getProvince());
        assertEquals("", emptyInfo.getCity());
        assertEquals("", emptyInfo.getIsp());

        // Test plain string case
        IpInfo plainInfo = IpInfo.fromString("3.3.3.3", "Unknown location");
        assertEquals("3.3.3.3", plainInfo.getIp());
        assertEquals("", plainInfo.getCountry());
        assertEquals("", plainInfo.getRegion());
        assertEquals("", plainInfo.getProvince());
        assertEquals("", plainInfo.getCity());
        assertEquals("Unknown location", plainInfo.getIsp());
    }

    @Test
    public void testIpInfoExtendedFields() {
        // Test new fields
        IpInfo info = new IpInfo();
        info.setAsn("AS4812");
        info.setAsnOwner("CHINANET-SH-AP");
        info.setLongitude(121.474);
        info.setLatitude(31.2304);
        info.setTimezone("Asia/Shanghai");
        info.setUsageType("corporate");
        info.setNativeIp(true);
        info.setRisk("low");
        info.setProxy(false);
        info.setCrawlerName("-");

        assertEquals("AS4812", info.getAsn());
        assertEquals("CHINANET-SH-AP", info.getAsnOwner());
        assertEquals(121.474, info.getLongitude());
        assertEquals(31.2304, info.getLatitude());
        assertEquals("Asia/Shanghai", info.getTimezone());
        assertEquals("corporate", info.getUsageType());
        assertEquals(true, info.getNativeIp());
        assertEquals("low", info.getRisk());
        assertEquals(false, info.getProxy());
        assertEquals("-", info.getCrawlerName());
    }

    @Test
    public void testLocalIp2RegionResolver() throws IOException {
        // Create a mock resolver for testing
        LocalIp2RegionResolver resolver = new LocalIp2RegionResolver(
                null, "TestResolver", 50);

        assertEquals("TestResolver", resolver.getName());
        assertEquals(50, resolver.getWeight());
        assertEquals(1.0, resolver.getSuccessRate(), 0.001);
        assertTrue(resolver.isAvailable());
    }

    @Test
    public void testGeoIP2ResolverClassExists() {
        // Just test that the GeoIP2Resolver class exists
        assertNotNull(GeoIP2Resolver.class);
    }

    @Test
    public void testTryLoadLocalSources() {
        // 测试自动加载本地数据源功能
        List<IpSource> sources = IpQueryEngineFactory.tryLoadLocalSources();
        // 由于我们没有实际的数据库文件，这里应该是空列表
        assertNotNull(sources);
    }

    @Test
    public void testTryLoadGeoIpSource() {
        // 测试尝试加载GeoIP数据源
        Optional<IpSource> source = IpQueryEngineFactory.tryLoadGeoIpSource();
        // 由于我们没有实际的数据库文件和依赖，这里应该返回empty
        assertFalse(source.isPresent());
    }

    @Test
    public void testTryLoadIp2RegionSource() {
        // 测试尝试加载ip2region数据源
        Optional<IpSource> source = IpQueryEngineFactory.tryLoadIp2RegionSource();
        // 由于我们没有实际的数据库文件和依赖，这里应该返回empty
        assertFalse(source.isPresent());
    }

    @Test
    public void testIpQueryEngineFactory() {
        // Just test that the factory class exists
        assertNotNull(IpQueryEngineFactory.class);
    }

    @Test
    public void testIpQueryEngineWithMockSources() throws Exception {
        // Create mock IP sources
        List<IpSource> sources = new ArrayList<>();

        // Test engine creation
        IpQueryEngine engine = IpQueryEngineFactory.createFromSources(sources);
        assertNotNull(engine);
    }


    @Test
    public void  testFreeApiEngine() throws Exception {
        IpQueryEngine ipQueryEngine = IpQueryEngineFactory.createFreeApiEngine(1024, Duration.ofSeconds(60), Duration.ofSeconds(30));
        ipQueryEngine.addSource(new LocalIp2RegionResolver("ip2region_v4.xdb", null,true,false,"ip2region",1));
        // 读取同级目录下文件名为 test_ip.txt的内容
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("test_ip.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 使用线程池并发处理IP查询任务
        int size = lines.size();
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            executorService.execute(() -> {
                String ip = lines.get(finalI);
                try {
                    IpInfo result = ipQueryEngine.query(ip);
                    System.out.println("第" + finalI + "次查询结果：" + result.toString());
                } catch (Exception e) {
                    System.out.println("第" + finalI + "次查询异常：" + e.getMessage());
                }
                countDownLatch.countDown();
            });
        }

        // 等待所有任务完成
        try {
            countDownLatch.await();
        } finally {
            executorService.shutdown();
        }

        System.out.println(">>>>>>>>>>>>> 查询结束 >>>>>>>>>>>>>");
        AggregatedMetrics aggregatedMetrics = ipQueryEngine.getAggregatedMetrics();
        System.out.println("缓存统计：" + aggregatedMetrics.getCacheStats());
        System.out.println("执行次数：" + aggregatedMetrics.getTotalMetrics().getExecutionCount());
        System.out.println("成功次数：" + aggregatedMetrics.getTotalMetrics().getResponseCount());
        System.out.println("失败次数：" + aggregatedMetrics.getTotalMetrics().getFailureCount());
        System.out.println("总耗时：" + aggregatedMetrics.getTotalMetrics().getTotalResponseTime());
        System.out.println("平均耗时：" + aggregatedMetrics.getTotalMetrics().getAverageResponseTime());
        System.out.println("成功率：" + aggregatedMetrics.getTotalMetrics().getSuccessRate());
        System.out.println(">>>>>>>>>>>>> 本地数据源统计 >>>>>>>>>>>>>");
        AggregatedMetrics.DataSourceMetrics localMetrics = aggregatedMetrics.getLocalMetrics();
        if (localMetrics.getAllSources() != null && !localMetrics.getAllSources().isEmpty()) {
            localMetrics.getAllSources().forEach(source -> {
                System.out.println("数据源名称：" + source.getName());
                System.out.println("数据源执行次数：" + source.getExecutionCount());
                System.out.println("数据源成功次数：" + (source.getExecutionCount() - source.getFailureCount()));
                System.out.println("数据源失败次数：" + source.getFailureCount());
                System.out.println("数据源总耗时：" + source.getTotalResponseTime());
                System.out.println("数据源成功率：" + source.getSuccessRate());
                System.out.println("数据源平均耗时：" + source.getAverageResponseTime() + "ms");
            });
        } else {
            System.out.println("数据源名称：ip2region");
            System.out.println("数据源执行次数：" + localMetrics.getExecutionCount());
            System.out.println("数据源成功次数：" + (localMetrics.getExecutionCount() - localMetrics.getFailureCount()));
            System.out.println("数据源失败次数：" + localMetrics.getFailureCount());
            System.out.println("数据源总耗时：" + localMetrics.getTotalResponseTime());
            System.out.println("数据源成功率：" + localMetrics.getSuccessRate());
            System.out.println("数据源平均耗时：" + localMetrics.getAverageResponseTime() + "ms");
        }

        System.out.println(">>>>>>>>>>>>> 网络数据源统计 >>>>>>>>>>>>>");
        aggregatedMetrics.getNetworkMetrics().getAllSources().forEach(sourceMetrics -> {
            System.out.println("数据源名称：" + sourceMetrics.getName());
            System.out.println("数据源执行次数：" + sourceMetrics.getExecutionCount());
            System.out.println("数据源成功次数：" + (sourceMetrics.getExecutionCount() - sourceMetrics.getFailureCount()));
            System.out.println("数据源失败次数：" + sourceMetrics.getFailureCount());
            System.out.println("数据源总耗时：" + sourceMetrics.getTotalResponseTime());
            System.out.println("数据源成功率：" + sourceMetrics.getSuccessRate());
            System.out.println("数据源平均耗时：" + sourceMetrics.getAverageResponseTime() + "ms");
        });
    }
}
