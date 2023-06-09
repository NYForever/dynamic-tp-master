
# 源码解读

## 1.入口注解 && DtpExecutor对象

**以下过程主要是解释了通过@EnableDynamicTp注解，将配置中的线程池对象加载到spring中；
以及通过@Bean注入DtpExecutor对象，默认开启监控功能**

- 1.`@EnableDynamicTp`
  - 1.通过Import注解引入了类`DtpBeanDefinitionRegistrar`
  - 2.加载配置文件，将配置文件中配置的线程池bean注入到spring容器中
- 2.在config文件中，通过@Bean注入`DtpExecutor`对象
  - 1.`DtpExecutor`其实现了`InitializingBean`接口，会在实例化过程中设置监控相关的信息，所以`DtpExecutor`执行器默认开启监控功能
  - 2.任务触发拒绝策略时，会通过动态代理生成不同的拒绝策略，代码在`DtpExecutor`的构造方法中`RejectHandlerGetter.getProxy`
    - 1.最终的拒绝实现由`RejectedInvocationHandler.invoke`方法来执行，
    - 2.`RejectedInvocationHandler`实现了`RejectedAware`接口，**在拒绝发生时，会调用`beforeReject(executor);`方法触发告警**
  - 3.在任务执行前，会调用`beforeExecute`，**检查入队列是否超时，超时触发告警**
  - 4.在任务执行过程中，重写了`execute`，**如果配置了`taskWrappers`包装器，可以在任务执行过程中做增强**
  - 5.在任务执行后，会调用`afterExecute`，**检查任务执行是否超时，超时触发告警**


## 2.配置变更，刷新spring中的DtpExecutor对象
> 不同的配置中心，配置变更逻辑不一样，因此根据不同的配置中心，实现了多个starter，我们从starter看起
> 

- 1.这里以`dynamic-tp-spring-boot-starter-nacos`为例进行分析，其他starter也类似，只是触发配置变更的方式不一样
  - 1.通过配置`spring.factories`指定了配置类，spring启动即会加载该配置类`DtpAutoConfiguration`
  - 2.该配置类引入了base配置类`BaseBeanAutoConfiguration`
    - 1.在其中引入了**后置处理器`DtpPostProcessor`**
    - 2.其会在每个bean初始化之后，校验是否需要注册到`DtpRegistry`对象的map中，用于统一管理（在此处做校验，好过spring都启动完成后，再依次拿出bean做校验要好）
    - 3.其注入了`DtpRegistry`对象（属于core包下的核心类），`DtpRegistry`又实现了`ApplicationRunner`接口，会在spring容器启动完成之后，执行其`run`方法，打印当前注入到容器的线程池name
  - 3.配置刷新，更新线程池对象，`NacosRefresher`来实现
    - 1.其实现了`InitializingBean`对象，在初始化该bean之后会执行`afterPropertiesSet`方法
    - 2.通过配置文件获取要连接的nacos地址
    - 3.并使用`configService`监听，即所监听的nacos配置如果有变化，就会触发监听
    - 4.配置变化会触发`receiveConfigInfo`方法（此逻辑属于nacos配置中心），将最新的配置信息推送过来
    - 5.最终会调用父类`AbstractRefresher`的`refresh`方法
    - 6.根据配置文件的类型，获取对应的`ConfigHandler`对象，解析配置文件为map
    - 7.将map数据绑定到`DtpProperties`对象中
    - 8.调用`DtpRegistry`的刷新方法，真正刷新线程池的配置，同时发出配置变更事件`RefreshEvent`，**_这里也是整个配置变更的核心_**
    - 9.后续逻辑是各中间件监听该事件，比如tomcat、dubbo等，获取到最新配置，看是否是要变更这些中间件的线程池配置









<p align="center">
	<img alt="logo" src="https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/119d7277b1c747479d5980825a078912~tplv-k3u1fbpfcp-zoom-1.image" width="50%">
</p>
<p align="center">
	<strong>基于配置中心的轻量级动态线程池，内置监控告警功能，可通过SPI自定义扩展实现</strong>
</p>

<p align="center">
  <a href="https://gitee.com/dromara/dynamic-tp"><img src="https://gitee.com/dromara/dynamic-tp/badge/star.svg"></a>
  <a href="https://gitee.com/dromara/dynamic-tp/members"><img src="https://gitee.com/dromara/dynamic-tp/badge/fork.svg"></a>
  <a href="https://github.com/dromara/dynamic-tp"><img src="https://img.shields.io/github/stars/dromara/dynamic-tp?style=flat-square&logo=github"></a>
  <a href="https://github.com/dromara/dynamic-tp/network/members"><img src="https://img.shields.io/github/forks/dromara/dynamic-tp?style=flat-square&logo=GitHub"></a>
  <a href="https://github.com/dromara/dynamic-tp/blob/master/LICENSE"><img src="https://img.shields.io/github/license/dromara/dynamic-tp.svg?style=flat-square"></a>
  <a target="_blank" href="https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image"><img src='https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ddfaed2cce2a47608fb0c0c375a10f08~tplv-k3u1fbpfcp-zoom-1.image' alt='备注加群'></a>
</p>

<p align="center">
    官网： <a href="https://dynamictp.cn">https://dynamictp.cn</a> 🔥
</p>

---

## 背景

**使用线程池 ThreadPoolExecutor 过程中你是否有以下痛点呢？**

> 1.代码中创建了一个 ThreadPoolExecutor，但是不知道那几个核心参数设置多少比较合适
>
> 2.凭经验设置参数值，上线后发现需要调整，改代码重启服务，非常麻烦
>
> 3.线程池相对开发人员来说是个黑盒，运行情况不能及时感知到，直到出现问题

如果你有以上痛点，动态可监控线程池（DynamicTp）或许能帮助到你。

如果看过 ThreadPoolExecutor 的源码，大概可以知道它对核心参数基本都有提供 set / get 方法以及一些扩展方法，可以在运行时动态修改、获取相应的值。

现在大多数的互联网项目其实都会微服务化部署，有一套自己的服务治理体系，微服务组件中的分布式配置中心扮演的就是动态修改配置，
实时生效的角色。那么我们是否可以结合配置中心来做运行时线程池参数的动态调整呢？答案是肯定的，而且配置中心相对都是高可用的，
使用它也不用过于担心配置推送出现问题这类事儿，而且也能减少研发动态线程池组件的难度和工作量。

**综上，可以总结出以下的背景**

- 广泛性：在 Java 开发中，想要提高系统性能，线程池已经是一个 90%以上的人都会选择使用的基础工具

- 不确定性：项目中可能会创建很多线程池，既有 IO 密集型的，也有 CPU 密集型的，但线程池的参数并不好确定；需要有套机制在运行过程中动态去调整参数

- 无感知性，线程池运行过程中的各项指标一般感知不到；需要有套监控报警机制在事前、事中就能让开发人员感知到线程池的运行状况，及时处理

- 高可用性，配置变更需要及时推送到客户端；需要有高可用的配置管理推送服务，配置中心是现在大多数互联网系统都会使用的组件，与之结合可以大幅度减少开发量及接入难度

---

## 简介

**基于以上背景分析，我们对线程池 ThreadPoolExecutor 做一些扩展增强，主要实现以下目标**

> 1.实现对运行中线程池参数的动态修改，实时生效
>
> 2.实时监控线程池的运行状态，触发设置的报警策略时报警，报警信息推送办公平台
>
> 3.定时采集线程池指标数据，配合像 grafana 这种可视化监控平台做大盘监控

**经过多个版本的迭代，目前最新版本具有以下特性** ✅

- **代码零侵入**：所有配置都放在配置中心，对业务代码零侵入

- **轻量简单**：基于 springboot 实现，引入 starter，接入只需简单4步就可完成，顺利3分钟搞定

- **高可扩展**：框架核心功能都提供 SPI 接口供用户自定义个性化实现（配置中心、配置文件解析、通知告警、监控数据采集、任务包装等等）

- **线上大规模应用**：参考[美团线程池实践](https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html)，美团内部已经有该理论成熟的应用经验

- **多平台通知报警**：提供多种报警维度（配置变更通知、活性报警、容量阈值报警、拒绝触发报警、任务执行或等待超时报警），已支持企业微信、钉钉、飞书报警，同时提供 SPI 接口可自定义扩展实现

- **监控**：定时采集线程池指标数据，支持通过 MicroMeter、JsonLog 日志输出、Endpoint 三种方式，可通过 SPI 接口自定义扩展实现

- **任务增强**：提供任务包装功能，实现TaskWrapper接口即可，如 MdcTaskWrapper、TtlTaskWrapper、SwTraceTaskWrapper，可以支持线程池上下文信息传递

- **兼容性**：JUC 普通线程池和 Spring 中的 ThreadPoolTaskExecutor 也可以被框架监控，@Bean 定义时加 @DynamicTp 注解即可

- **可靠性**：框架提供的线程池实现 Spring 生命周期方法，可以在 Spring 容器关闭前尽可能多的处理队列中的任务

- **多模式**：参考Tomcat线程池提供了 IO 密集型场景使用的 EagerDtpExecutor 线程池

- **支持多配置中心**：基于主流配置中心实现线程池参数动态调整，实时生效，已支持 Nacos、Apollo、Zookeeper、Consul、Etcd，同时也提供 SPI 接口可自定义扩展实现

- **中间件线程池管理**：集成管理常用第三方组件的线程池，已集成Tomcat、Jetty、Undertow、Dubbo、RocketMq、Hystrix等组件的线程池管理（调参、监控报警）

---

## 设计

**框架功能大体可以分为以下几个模块**

> 1.配置变更监听模块
>
> 2.服务内部线程池管理模块
>
> 3.三方组件线程池管理模块
>
> 4.监控模块
>
> 5.通知告警模块

![技术架构](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/38e4bf71d2c84b7ba67d7059b5432a7e~tplv-k3u1fbpfcp-zoom-1.image)

详细查看官网文档，[架构设计](https://dynamictp.cn/guide/introduction/architecture.html)

---

## 使用

- 接入步骤

  1.引入相应配置中心的依赖，具体见下述 maven依赖

  2.配置中心配置线程池实例，配置见下述（给出的是全配置项，不用的可以删除）

  3.启动类加 @EnableDynamicTp 注解

  4.使用 @Resource 或 @Autowired 进行依赖注入，或通过 DtpRegistry.getDtpExecutor("name")获取

  5.通过以上4步就可以使用了，是不是感觉超简单

- maven 依赖，见官网文档，[maven 依赖](https://dynamictp.cn/guide/use/maven.html)

- 线程池配置，见官网文档，[配置文件](https://dynamictp.cn/guide/use/config.html)

- 代码使用，见官网文档，[代码使用](https://dynamictp.cn/guide/use/code.html)

- 更详细使用实例请参考 `example` 工程

---

## 通知报警

- 触发报警阈值会推送相应报警消息（活性、容量、拒绝、任务等待超时、任务执行超时），且会高亮显示相应字段
  
  更多见官网文档，[通知报警](https://dynamictp.cn/guide/notice/alarm.html)

![告警](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d65151e3e9ca460eac18f30ea6be05d3~tplv-k3u1fbpfcp-zoom-1.image)

- 配置变更会推送通知消息，且会高亮变更的字段

![变更通知](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/30093a6ede914887bb5566139352fb8b~tplv-k3u1fbpfcp-zoom-1.image)


---

## 监控

![监控数据](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ec5a7d1a31e7418ba5d9a101a5c03826~tplv-k3u1fbpfcp-zoom-1.image)

通过 collectType 属性配置监控指标采集类型，默认 logging

- MicroMeter：通过引入相关 MicroMeter 依赖采集到相应的平台（如 Prometheus，InfluxDb...）

- Logging：定时采集指标数据以 Json 日志格式输出磁盘，
  地址 ${logPath}/dynamictp/${appName}.monitor.log

  ```bash
  {"datetime": "2022-04-17 11:35:15.208", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":4,"queueType":"VariableLinkedBlockingQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":6,"poolName":"dtpExecutor1"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"rejectHandlerName":"CallerRunsPolicy","queueCapacity":2000,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2000,"corePoolSize":2,"queueType":"TaskQueue","completedTaskCount":0,"dynamic":true,"maximumPoolSize":4,"poolName":"dtpExecutor2"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":0,"poolSize":0,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":0,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":1,"queueType":"LinkedBlockingQueue","completedTaskCount":0,"dynamic":false,"maximumPoolSize":1,"poolName":"commonExecutor"}}
  {"datetime": "2022-04-17 11:35:15.209", "app_name": "dynamic-tp-nacos-cloud-demo", "thread_pool_metrics": {"activeCount":0,"queueSize":0,"largestPoolSize":100,"poolSize":100,"queueCapacity":2147483647,"fair":false,"queueTimeoutCount":0,"rejectCount":0,"waitTaskCount":0,"taskCount":177,"runTimeoutCount":0,"queueRemainingCapacity":2147483647,"corePoolSize":100,"queueType":"TaskQueue","completedTaskCount":177,"dynamic":false,"maximumPoolSize":400,"poolName":"tomcatWebServerTp"}}
  ```

- 暴露 EndPoint 端点(dynamic-tp)，可以通过 http 方式请求
  ```json
  [
      {
          "dtp_name": "remoting-call",
          "core_pool_size": 6,
          "maximum_pool_size": 12,
          "queue_type": "SynchronousQueue",
          "queue_capacity": 0,
          "queue_size": 0,
          "fair": false,
          "queue_remaining_capacity": 0,
          "active_count": 0,
          "task_count": 21760,
          "completed_task_count": 21760,
          "largest_pool_size": 12,
          "pool_size": 6,
          "wait_task_count": 0,
          "reject_count": 124662,
          "reject_handler_name": "CallerRunsPolicy"
      }
  ]
  ```

---


## star趋势❤️

[![Stargazers over time](https://starchart.cc/dromara/dynamic-tp.svg)](https://starchart.cc/dromara/dynamic-tp)

---

## 知识星球

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/894a4e918ff14c13b4a66d3f30f7ff7e~tplv-k3u1fbpfcp-zoom-1.image)

---

## 联系我

看到这儿，**请给项目一个star**，你的支持是我们前进的动力！

使用过程中有任何问题，或者对项目有什么想法或者建议，可以加入社群，跟群友一起交流讨论。

微信群已满200人，可以关注微信公众号，加我个人微信拉群（备注：dynamic-tp）。

![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/530709dc29604630b6d1537d7c160ea5~tplv-k3u1fbpfcp-watermark.image)
