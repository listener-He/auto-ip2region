#  ⭐️Auto IP2Region ✨智能化的IP地理位置解析库
<div align="center">

  [![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
  [![Java](https://img.shields.io/badge/Java-17+-brightgreen.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  [![Release](https://img.shields.io/badge/release-v1.0.0-orange.svg)](https://github.com/listener-He/auto-ip2region/releases)
  [![GitHub](https://img.shields.io/github/license/listener-He/auto-ip2region?color=blue)](https://github.com/listener-He/auto-ip2region/blob/main/LICENSE)
  [![GitHub stars](https://img.shields.io/github/stars/listener-He/auto-ip2region)](https://github.com/listener-He/auto-ip2region/stargazers)
  [![GitHub issues](https://img.shields.io/github/issues/listener-He/auto-ip2region)](https://github.com/listener-He/auto-ip2region/issues)

</div>

<br/>

Auto IP2Region 是一个智能化的IP地址地理信息解析库，它结合了本地数据库和多个免费在线API服务，通过智能负载均衡和自动故障转移机制，为您提供准确、可靠的IP地理位置信息查询服务。

## 🎯 解决了什么问题？

在实际开发中，我们经常遇到以下痛点：

| 问题 | 传统解决方案 | Auto IP2Region 解决方案 |
|------|-------------|---------------------|
| 单一数据源不稳定 | 固定使用某个API | 多数据源智能切换            |
| 查询性能差 | 同步阻塞调用 | 热点缓存处理              |
| 限流导致失败 | 重试机制 | 智能负载均衡              |
| 数据准确性低 | 无法验证数据质量 | 基于成功率动态调整           |

## ✨ 核心特性

<div align="center">
  <img src="feature.png" alt="features" />
</div>

### ⚡ 高性能
- 基于[ip2region](https://github.com/lionsoul2014/ip2region)本地数据库，查询速度可达**微秒级**
- 基于[GeoLite2](https://dev.maxmind.com/geoip/geoip2/geolite2/) 本地数据库，查询速度可达**毫秒级**
- 使用负载均衡算法，支撑数据准确性
- 内置Guava Cache缓存机制，热点数据访问性能提升**80%+**

### 🔄 智能负载均衡
独特的加权负载均衡算法，综合考虑：
- **权重**(40%) - 静态配置优先级
- **成功率**(25%) - 动态历史成功率
- **执行次数**(20%) - 请求均匀分布
- **可用性**(15%) - 基于限流状态和响应时间评估

### 🔧 多数据源支持
| 类型    | 数据源 | 默认权重 | 特点 |
|-------|--------|------|-----|
| 本地数据库 | ip2region | 100  | 高性能，无需网络 |
| 本地数据库 | GeoIP2 | 100  | MaxMind GeoIP2数据库 |
| 免费API | 淘宝IP库 | 90   | 国内IP准确率高 |
| 免费API | ipapi.co | 80   | 国际IP覆盖广 |
| 免费API | 太平洋网络 | 50   | 太平洋网络API |
| 免费API | IP9 | 50   | 备用数据源 |
| 免费API | IPInfo | 50   | 附加信息丰富 |
| 免费API | XXLB | 50   | 备用数据源 |
| 免费API | Vore | 50   | 沃云API |
| 免费API | IP-MOE | 50   | IP-MOE API |
| 免费API | Zhengbingdong API | 10     | 50 |

### 🛡️ 自动故障转移
- 实时监控各数据源健康状况
- 自动降级到本地数据库
- 智能恢复机制，故障恢复后自动启用

### 📈 可观测性
- 实时统计各数据源成功率
- 缓存命中率监控
- 执行次数和限流情况跟踪
- 响应时间统计与分析

## 📊 数据源详细配置

### 本地数据源

| 配置项 | 默认值 | 说明 |
|-------|--------|-----|
| 限流速率 | 1000 QPS | 每秒查询数限制 |
| 权重 | 100 | 最高优先级 |
| 名称 | LocalIp2Region | 数据源标识 |

### 网络API数据源

| 数据源 | 限流速率(QPS) | 权重 | 名称 |
|--------|---------------|------|------|
| 淘宝IP库 | 3 | 90 | TaobaoAPI |
| ipapi.co | 2 | 80 | IpApiCo |
| 太平洋网络 | 1 | 50 | Pacific |
| IP9 | 1 | 50 | IP9 |
| IPInfo | 1 | 50 | IPInfo |
| XXLB | 1 | 50 | XXLB |
| Vore | 1 | 50 | Vore |
| IP-MOE | 1 | 50 | IP-MOE |

## 🧠 负载均衡算法详解

Auto IP2Region采用先进的加权负载均衡算法，综合考虑多个维度来选择最优的数据源：

### 算法公式

```text
最终得分 = 权重得分 × 0.4 + 成功率得分 × 0.25 + 执行次数得分 × 0.2 + 可用性得分 × 0.15
```

### 各维度计算方式

#### 1. 权重得分 (Weight Score)
```text
权重得分 = 数据源权重 / 所有可用数据源中的最高权重
```

#### 2. 成功率得分 (Success Rate Score)
```text
成功率得分 = 数据源历史成功率 (0-1之间)
```

#### 3. 执行次数得分 (Execution Count Score)
```text
执行次数得分 = 1 - (当前数据源执行次数 / 所有可用数据源的最大执行次数)
```

#### 4. 可用性得分 (Availability Score)
基于限流器的等待时间和平均响应时间评估数据源可用性：
- 无请求时：1.0
- 等待 < 10ms：0.9
- 等待 10-100ms：0.7
- 等待 100-500ms：0.5
- 等待 > 500ms：0.3
- 响应时间 < 50ms：1.0
- 响应时间 50-200ms：0.8
- 响应时间 200-500ms：0.6
- 响应时间 500-1000ms：0.4
- 响应时间 > 1000ms：0.2

综合可用性评估：`可用性得分 = 限流等待时间得分 × 0.6 + 响应时间得分 × 0.4`

### 轮询示例

假设有以下三个数据源：

| 数据源 | 权重 | 成功率 | 执行次数 | 可用性 |
|--------|------|--------|----------|--------|
| 本地数据库 | 100 | 0.99 | 1000 | 1.0 |
| 淘宝API | 90 | 0.95 | 500 | 0.9 |
| ipapi.co | 80 | 0.85 | 200 | 0.7 |

计算各数据源得分：

1. **本地数据库**：
   - 权重得分 = 100/100 = 1.0
   - 成功率得分 = 0.99
   - 执行次数得分 = 1 - (1000/1000) = 0
   - 可用性得分 = 1.0
   - 最终得分 = 1.0×0.4 + 0.99×0.25 + 0×0.2 + 1.0×0.15 = 0.7975

2. **淘宝API**：
   - 权重得分 = 90/100 = 0.9
   - 成功率得分 = 0.95
   - 执行次数得分 = 1 - (500/1000) = 0.5
   - 可用性得分 = 0.9
   - 最终得分 = 0.9×0.4 + 0.95×0.25 + 0.5×0.2 + 0.9×0.15 = 0.8075

3. **ipapi.co**：
   - 权重得分 = 80/100 = 0.8
   - 成功率得分 = 0.85
   - 执行次数得分 = 1 - (200/1000) = 0.8
   - 可用性得分 = 0.7
   - 最终得分 = 0.8×0.4 + 0.85×0.25 + 0.8×0.2 + 0.7×0.15 = 0.7925

根据得分排序，选择淘宝API作为本次查询的数据源。

## 📊 聚合指标监控

系统提供全面的聚合指标监控功能，可实时查看系统运行状态：

### 指标分类
1. **本地数据源指标**：执行次数、失败次数、成功率
2. **网络数据源指标**：执行次数、失败次数、成功率、平均响应时间
3. **缓存指标**：缓存大小、缓存命中率等统计信息
4. **各数据源详细指标**：每个数据源的独立统计信息

### 使用示例
```java
// 获取聚合指标
AggregatedMetrics metrics = engine.getAggregatedMetrics();

// 查看本地数据源指标
DataSourceMetrics localMetrics = metrics.getLocalMetrics();
System.out.println("本地数据源执行次数: " + localMetrics.getExecutionCount());

// 查看网络数据源指标
DataSourceMetrics networkMetrics = metrics.getNetworkMetrics();
System.out.println("网络数据源平均响应时间: " + networkMetrics.getAverageResponseTime());

// 查看缓存指标
System.out.println("缓存大小: " + metrics.getCacheSize());
```

## 🚀 快速开始

### 📦 Maven依赖 (等待发布)

```xml
<dependency>
    <groupId>cn.hehouhui</groupId>
    <artifactId>auto-ip2region</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 📥 下载数据库文件

使用本库前，需要下载相应的数据库文件

**GeoIP2数据库可以从MaxMind官网免费获取**：

1. 访问 [MaxMind GeoLite2](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)
2. 注册账号并登录
3. 下载 GeoLite2 City 数据库
4. 解压获得 `GeoLite2-City.mmdb` 文件

**ip2region从github仓库下载**
- 下载地址：[https://github.com/lionsoul2014/ip2region/tree/master/data](https://github.com/lionsoul2014/ip2region/tree/master/data)
- IP_V4 文件名：`ip2region_v4.xdb`
- IP_V6 文件名：`ip2region_v6.xdb`

> 注意：ip2region和GeoIP2数据库为可选依赖，只有在使用对应的本地解析器时才需要添加相关依赖和数据库文件 数据库需要定期更新以保证准确性。

### 💡 基本用法

#### 1. 使用本地ip2region数据库

```java
// 创建只包含本地ip2region数据源的查询引擎
IpQueryEngine engine = IpQueryEngineFactory.createLocalEngine(null, null, null);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 2. 使用GeoIP2本地数据库

```java
// 创建只包含GeoIP2数据源的查询引擎
IpQueryEngine engine = IpQueryEngineFactory.createLocalEngine(null, null, null);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 3. 自动从资源目录加载本地数据库

```java
// 自动尝试从资源目录加载所有可用的本地数据源
List<IpSource> localSources = IpQueryEngineFactory.tryLoadLocalSources();

// 如果找到了本地数据源，则创建引擎
if (!localSources.isEmpty()) {
    IpQueryEngine engine = IpQueryEngineFactory.createFromSources(localSources);
    
    // 查询IP信息
    try {
        IpInfo info = engine.query("8.8.8.8");
        System.out.println(info);
    } catch (Exception e) {
        e.printStackTrace();
    }
} else {
    System.out.println("未找到可用的本地数据库");
}
```

#### 4. 使用免费API数据源

```java
// 创建包含所有免费API数据源的查询引擎
IpQueryEngine engine = IpQueryEngineFactory.createFreeApiEngine(null, null, null);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 5. 混合使用本地数据库和API

```java
// 创建混合数据源的查询引擎（本地+所有免费API）
IpQueryEngine engine = IpQueryEngineFactory.createAllSourceEngine(false, null, null, null);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

### 🔧 自定义HTTP请求处理器

```java
// 创建自定义HTTP请求处理器
HttpRequestHandler customHandler = new CustomHttpRequestHandler();

// 创建包含所有免费API数据源的查询引擎，使用自定义HTTP请求处理器
List<IpSource> sources = IpQueryEngineFactory.loadFreeApiSources(customHandler, false);
IpQueryEngine engine = IpQueryEngineFactory.createFromSources(sources);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

### 🧩 自定义数据源

```java
// 创建自定义数据源列表
List<IpSource> sources = new ArrayList<>();
sources.add(new LocalIp2RegionResolver(searcher, "LocalResolver", 100));
sources.add(new TaobaoIpResolver(3, "TaobaoResolver", 90));

// 创建查询引擎
IpQueryEngine engine = IpQueryEngineFactory.createFromSources(sources);

// 查询IP信息
try {
    IpInfo info = engine.query("8.8.8.8");
    System.out.println(info);
} catch (Exception e) {
    e.printStackTrace();
}
```

## 📊 性能对比

经过测试，在相同环境下不同方案的性能对比如下：

| 方案          | 平均响应时间 | 并发处理能力   | 准确率 |
|-------------|------------|----------|-------|
| 纯API调用      | 320ms | 2,0 QPS  | 92% |
| 纯本地数据库      | 0.05ms | 200 QPS  | 95% |
| API + 本地数据库 | 0.1ms | 2,00 QPS | 98% |


## 🔌 扩展指南

### 添加新的IP数据源

要添加一个新的IP数据源，只需继承[AbstractIpSource](src/main/java/cn/hehouhui/ip2region/core/AbstractIpSource.java)类并实现必要的方法：

```java
public class MyCustomIpResolver extends AbstractIpSource {
    
    public MyCustomIpResolver(double rate, String name, int weight) {
        super(rate, name, weight);
    }
    
    @Override
    protected IpInfo doQuery(String ip) throws Exception {
        // 实现具体的IP查询逻辑
        // 返回IpInfo对象
        return new IpInfo(/* 参数 */);
    }
}
```

### 自定义负载均衡策略

实现[LoadBalancer](src/main/java/cn/hehouhui/ip2region/loadbalancer/LoadBalancer.java)接口来创建自己的负载均衡算法：

```java
public class CustomLoadBalancer implements LoadBalancer {
    
    @Override
    public IpSource select(List<IpSource> sources) {
        // 实现自定义的选择逻辑
        return sources.get(0); // 示例
    }
}
```

## 📚 API文档

详细API文档请参考[API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## 🤝 贡献

欢迎任何形式的贡献！如果您有任何建议或发现了bug，请提交[Issue](https://github.com/listener-He/auto-ip2region/issues)或者发起[Pull Request](https://github.com/listener-He/auto-ip2region/pulls)。

### 开发环境搭建

1. 克隆项目：`git clone https://github.com/listener-He/auto-ip2region.git`
2. 导入IDE：使用IntelliJ IDEA或Eclipse导入Maven项目
3. 构建项目：`mvn clean install`

## 📄 许可证

本项目采用Apache License 2.0许可证，详情请见[LICENSE](LICENSE)文件。

## 💬 联系方式

如有任何问题，请联系：
- 邮箱：hehouhui@foxmail.com
- GitHub Issues：[提交问题](https://github.com/listener-He/auto-ip2region/issues)

---
<div align="center">
  Made with ❤️ by Honesty | © 2025 All rights reserved
</div>
