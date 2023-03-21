---
title: 发版记录
icon: note
order: 1
author: yanhom
date: 2022-06-11
category:
  - 发版记录
sticky: false
star: true
---

::: tip

## v1.0.8 发布记录

距离 v1.0.7 发布已经有差不多 2 个月时间，这个版本新增了好些功能，同时优化重构了一些代码设计，欢迎大家升级体验哦！


#### Features

+ 新增内存安全队列 MemorySafeLinkedBlockingQueue，感谢 @dragon-zhang 提供实现

+ WebServer 线程池管理支持 Reactive 环境下使用，感谢 @abbottliu.liu 提供实现

+ 支持 Dubbox 线程池管理，感谢 @Redick01 提供实现

+ 支持 Spring 中的 ThreadPoolTaskExecutor 线程池管理，感谢 @Redick01 提供实现

+ 支持 Etcd 配置中心接入，感谢 @Redick01 提供实现

+ 三方中间件线程池通知告警支持别名配置，感谢 @renbiao002 提供实现

+ 三方中间件线程池管理支持通知告警功能

+ 新增 MdcTaskWrapper 任务包装器，支持 MDC 上下文传递

+ 新增 SwTraceTaskWrapper 任务包装器，支持 Skywalking TID 传递

+ 监控数据输出新增输出到应用日志中的 collector

+ 指标数据采集支持配置多个采集方式

+ 新增通知告警集群限流插件，见 starter-extension-limiter-redis 模块

+ ThreadPoolCreator 类新增一些内存安全快捷创建线程池方法


#### BugFix

+ 兼容 JDK11 当前要设置核心线程数不能大于上次设置的最大线程数限制

+ 修复核心线程预热设置 preStartAllCoreThreads 不生效问题

+ 修复 Hystrix 线程池获取失败 & 调参被覆盖问题


#### Refactor

+ 重构 logging 模块，去掉事件监听依赖

+ 重构抽象 adapter 模块代码

+ 责任链模式重构 notify 模块


#### Optimize

+ example 添加 Hystrix 线程池的测试例子

+ 低版本 Apollo 配置文件格式兼容

+ Undertow 容器开启活跃线程池数获取功能

+ Endpoint 端点接口支持三方中间件线程池状态获取

+ 优化三方中间件参数刷新逻辑，增加校验判断逻辑

:::


::: tip

## v1.0.7 发布记录

明细：https://juejin.cn/post/7108551236609114149

距离v1.0.5发布已经有差不多2个月时间，这个版本框架源码调整还是比较大的，重构了好一些功能， 主要是围绕第三方中间件线程池集成来改动的，是向前兼容的，同时修复了三个小bug。

如果你有下述痛点，快快升级体验吧。

1）如果你在使用 Dubbo，那么你大概率遇到过 Dubbo 线程池耗尽的情况，是不是很烦恼呢？尝试使用下 DynamicTp 的 Dubbo 线程池管理功能，结合告警、实时监控、动态调参等功能尽可能降低 Dubbo 线程池耗尽，请求拒绝的风险。

2）mq 应该是很多互联网系统都会使用到的中间件，使用 mq 经常会遇到的其中一个问题就是消息积压问题，具体啥原因导致积压需要具体问题具体分析，就RocketMq来说，消费端是使用线程池进行处理消息的，所以说线程池的设置也会直接或者间接影响到消费速度，需要对此进行监控、告警、以及动态调参，尽可能降低因线程池设置而导致的积压风险。

注意：三方组件的包需要自己引入，具体查看官网


#### Features

+ 报警渠道接入飞书

+ 支持 Apache Dubbo & Alibab Dubbo 服务端提供端线程池管理

+ 支持 RocketMq 消费端线程池管理

+ 支持 Hystrix 线程池管理

+ 支持 SpringBoot 内置三大WebServer（Tomcat、Jetty、Undertow）线程池管理

+ 增加线程池别名配置，提升告警信息可读易懂性

+ 提供任务包装类NamedRunable，提交任务时设置标识名称，方便问题追踪

+ 告警项自定义配置，不配置的项用默认值


#### BugFix

+ 修复并发导致通知报警信息发送多条的问题

+ 修复通知渠道配置修改不能动态更新问题

+ 修复钉钉手机端报警信息高亮失效问题


#### Refactor

+ 重构部分通知告警模块实现，支持三方中间件通知告警

+ 重构调整 adapter、starter 模块代码组织结构

:::

::: tip

## v1.0.5

### Features

+ logging模块添加log4j2支持

+ 配置文件支持json格式，zk已支持json、properties格式配置


### BugFix

+ [#I54B4R](https://gitee.com/dromara/dynamic-tp/issues/I54B4R)  


### Refactor

+ 部分代码优化

### Dependency

+ transmittable-thread-local升级到2.12.6

+ micrometer升级到1.8.5

:::

::: tip

## v1.0.4

### Features

+ 配置中心支持Consul

+ 监控告警模块增加任务排队等候超时、任务执行超时监控告警

+ 线程池完全配置在配置中心，无需代码编程式配置，服务启动会自动创建线程池实例，交给Spring容器管理

+ 拒绝策略告警优化，支持前后告警间隔计数

+ 相关代码优化

:::

::: tip

## v1.0.3

### Features

+ 配置中心支持Zookeeper

+ 线程池交由Spring管理其生命周期，可以通过依赖注入方式使用

+ 创建时添加@DynamicTp注解支持监控JUC原生线程池

+ 仿照Tomcat线程池设计，提供IO密集型线程池（EagerDtpExecutor）

+ 相关代码优化，增加必要校验

:::

::: tip

## v1.0.2

### Features

+ 配置中心支持Nacos、Apollo、Zookeeper

+ 告警平台支持企微、钉钉

+ 监控指标数据采集支持json日志输出、MicorMeter以及Endpoint三种方式

+ 第三方组件线程池管理已集成SpringBoot内置三大WebServer（Tomcat、Jetty、Undertow）

+ 核心模块都提供SPI接口可供自定义扩展（配置中心、配置文件解析、告警平台、监控指标数据采集）

+ 提供完整使用示例（包含Grafana配置面板Json文件，直接import即可使用）

:::