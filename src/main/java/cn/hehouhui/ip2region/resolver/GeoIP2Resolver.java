package cn.hehouhui.ip2region.resolver;

import cn.hehouhui.ip2region.IpInfo;
import cn.hehouhui.ip2region.core.AbstractIpSource;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * GeoIP2本地数据库解析器，基于MaxMind GeoIP2数据库实现。
 *
 * @author HeHui
 * @date 2025-12-02
 */
public class GeoIP2Resolver extends AbstractIpSource {
    private final DatabaseReader reader;

    /**
     * 构造函数
     *
     * @param dbFile           GeoIP2数据库文件
     * @param name             解析器名称
     * @param weight           解析器权重
     * @throws IOException 文件读取异常
     */
    public GeoIP2Resolver(File dbFile, String name, int weight) throws IOException {
        super(name, weight);
        this.reader = new DatabaseReader.Builder(dbFile).build();
    }

    /**
     * 构造函数
     *
     * @param reader           GeoIP2数据库读取器
     * @param name             解析器名称
     * @param weight           解析器权重
     */
    public GeoIP2Resolver(DatabaseReader reader, String name, int weight) {
        super(name, weight);
        this.reader = reader;
    }

    @Override
    public IpInfo query(String ip) throws Exception {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIp(ip);
            
            // 填充基本地理信息
            if (response.getCountry() != null) {
                ipInfo.setCountry(response.getCountry().getName());
            }
            
            if (response.getSubdivisions() != null && !response.getSubdivisions().isEmpty()) {
                ipInfo.setProvince(response.getSubdivisions().get(0).getName());
            }
            
            if (response.getCity() != null) {
                ipInfo.setCity(response.getCity().getName());
            }
            
            // 填充ISP信息
            if (response.getTraits() != null) {
                ipInfo.setIsp(response.getTraits().getIsp());
                ipInfo.setAsn(String.valueOf(response.getTraits().getAutonomousSystemNumber()));
                ipInfo.setAsnOwner(response.getTraits().getAutonomousSystemOrganization());
            }
            
            // 填充经纬度信息
            if (response.getLocation() != null) {
                ipInfo.setLatitude(response.getLocation().getLatitude());
                ipInfo.setLongitude(response.getLocation().getLongitude());
                ipInfo.setTimezone(response.getLocation().getTimeZone());
            }
            
            updateSuccessStats();
            return ipInfo;
        } catch (IOException | GeoIp2Exception e) {
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
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}