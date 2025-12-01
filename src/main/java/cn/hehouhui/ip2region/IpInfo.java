package cn.hehouhui.ip2region;

import java.util.Objects;

/**
 * IP地址信息模型类，用于封装IP地址的地理位置信息。
 *
 * @author HeHui
 * @date 2025-12-01
 */
public class IpInfo {
    /**
     * IP地址
     */
    private String ip;
    /**
     * 国家
     */
    private String country;
    /**
     * 地区
     */
    private String region;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * ISP运营商
     */
    private String isp;

    /**
     * 默认构造函数
     */
    public IpInfo() {
    }

    /**
     * 带参构造函数
     *
     * @param ip       IP地址
     * @param country  国家
     * @param region   地区
     * @param province 省份
     * @param city     城市
     * @param isp      ISP运营商
     */
    public IpInfo(String ip, String country, String region, String province, String city, String isp) {
        this.ip = ip;
        this.country = country;
        this.region = region;
        this.province = province;
        this.city = city;
        this.isp = isp;
    }

    /**
     * 从字符串创建IpInfo对象
     *
     * @param ip           IP地址
     * @param regionString 区域信息字符串
     *
     * @return IpInfo对象
     */
    public static IpInfo fromString(String ip, String regionString) {
        if (regionString == null || regionString.isEmpty()) {
            return new IpInfo(ip, "", "", "", "", "");
        }

        // Handle ip2region format: country|region|province|city|isp
        if (regionString.contains("|")) {
            String[] parts = regionString.split("\\|");
            if (parts.length == 5) {
                return new IpInfo(
                    ip,
                    parts[0].equals("0") ? "" : parts[0],
                    parts[1].equals("0") ? "" : parts[1],
                    parts[2].equals("0") ? "" : parts[2],
                    parts[3].equals("0") ? "" : parts[3],
                    parts[4].equals("0") ? "" : parts[4]
                );
            }
        }

        // Handle other formats as plain region info
        return new IpInfo(ip, "", "", "", "", regionString);
    }

    /**
     * 获取IP地址
     *
     * @return IP地址
     */
    public String getIp() {
        return ip;
    }

    /**
     * 设置IP地址
     *
     * @param ip IP地址
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 获取国家
     *
     * @return 国家
     */
    public String getCountry() {
        return country;
    }

    /**
     * 设置国家
     *
     * @param country 国家
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * 获取地区
     *
     * @return 地区
     */
    public String getRegion() {
        return region;
    }

    /**
     * 设置地区
     *
     * @param region 地区
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 获取省份
     *
     * @return 省份
     */
    public String getProvince() {
        return province;
    }

    /**
     * 设置省份
     *
     * @param province 省份
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 获取城市
     *
     * @return 城市
     */
    public String getCity() {
        return city;
    }

    /**
     * 设置城市
     *
     * @param city 城市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取ISP运营商
     *
     * @return ISP运营商
     */
    public String getIsp() {
        return isp;
    }

    /**
     * 设置ISP运营商
     *
     * @param isp ISP运营商
     */
    public void setIsp(String isp) {
        this.isp = isp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpInfo ipInfo = (IpInfo) o;
        return Objects.equals(ip, ipInfo.ip) &&
            Objects.equals(country, ipInfo.country) &&
            Objects.equals(region, ipInfo.region) &&
            Objects.equals(province, ipInfo.province) &&
            Objects.equals(city, ipInfo.city) &&
            Objects.equals(isp, ipInfo.isp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, country, region, province, city, isp);
    }

    @Override
    public String toString() {
        return "IpInfo{" +
            "ip='" + ip + '\'' +
            ", country='" + country + '\'' +
            ", region='" + region + '\'' +
            ", province='" + province + '\'' +
            ", city='" + city + '\'' +
            ", isp='" + isp + '\'' +
            '}';
    }
}
