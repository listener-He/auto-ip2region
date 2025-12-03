package cn.hehouhui.ip2region;

import cn.hehouhui.ip2region.core.IpSource;
import cn.hehouhui.ip2region.resolver.GeoIP2Resolver;
import cn.hehouhui.ip2region.resolver.LocalIp2RegionResolver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        IpQueryEngine ipQueryEngine = IpQueryEngineFactory.createFreeApiEngine(100, Duration.ofSeconds(60), Duration.ofSeconds(30));
        IpInfo query1 = ipQueryEngine.query("");
        System.out.println(query1);
        IpInfo query2 = ipQueryEngine.query("");
        System.out.println(query2);
        IpInfo query3 = ipQueryEngine.query("");
        System.out.println(query3);
    }
}
