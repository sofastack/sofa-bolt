# SOFABolt Project

[![Build Status](https://travis-ci.org/alipay/sofa-bolt.svg?branch=master)](https://travis-ci.org/alipay/sofa-bolt)
[![Coverage Status](https://coveralls.io/repos/github/alipay/sofa-bolt/badge.svg?branch=master)](https://coveralls.io/github/alipay/sofa-bolt)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![maven](https://img.shields.io/badge/maven-v0.1.0-blue.svg)

# 1. 介绍
SOFABolt 是蚂蚁金融服务集团开放的基于Netty开发的网络通信框架。
* 为了让java程序员，花更多的时间在一些productive的事情上，而不是纠结底层NIO的实现，处理难以调试的网络问题，netty应运而生；
* 为了让中间件的开发者，花更多的时间在中间件的特性实现上，而不是重复地一遍遍制造通信框架的轮子，bolt应运而生。

bolt名字取自迪士尼动画，闪电狗，定位是一个基于netty最佳实践过的，通用、高效、稳定的通信框架。
我们希望能把这些年，在rpc，msg在网络通信上碰到的问题与解决方案沉淀到这个基础组件里，不断的优化和完善它。让更多的需要网络通信的场景能够统一受益。
目前已经运用在了蚂蚁中间件的微服务([SOFARPC](https://github.com/alipay/sofa-rpc))，消息中心，分布式事务，分布式开关，配置中心等众多产品上。

# 2. 功能介绍
![intro](./.middleware-common/intro.png)

## SOFABolt的基础功能包括
* 基础通信功能(remoting-core)
    * 网络IO与线程模型
    * 连接管理(无锁建连,定时断链,自动重连)
    * 基础通信模型(oneway,sync,future,callback)
    * 超时控制
    * 批量解包与批量提交处理器
    * 心跳与IDLE事件处理
* 协议框架(protocol-skeleton)
    * 命令与命令处理器
    * 编解码处理器
    * 心跳触发器
* 私有协议定制实现-RPC特征的通信协议(protocol-implementation)
    * RPC通信协议的设计
    * 灵活的反序列化时机控制
    * 请求处理超时FailFast机制
    * 用户请求处理器(UserProcessor)
    * 双工通信
    
## 用法1
将SOFABolt用作一个远程通信框架,使用者可以不关心通信协议的定义细节,直接复用我们的RPC特征的通信协议。然后简单的实现客户端与服务端的初始化和启动逻辑,同时注册一个用户请求处理器,即可完成远程调用。如下图所示:
同时,我们的其他基础功能像连接管理,心跳等特性,都默认可以使用。

![invoke_type](./.middleware-common/invoke_types.png)

* 简单的客户端与服务端通信[示例demo](https://github.com/alipay/sofa-bolt/tree/master/src/test/java/com/alipay/remoting/demo)

## 用法2
将SOFABolt用作一个协议框架,使用者可以复用基础的通信模型,协议包含的接口定义等基础功能。然后根据自己的私有协议需求,定义一整套Command类型,Command处理器。

# 4. 如何贡献
开放代码允许在签署协议之后,提交贡献代码.
具体参考:[如何参与SOFABolt代码](./CONTRIBUTING.md)

# 5. 版权协议
对SOFABolt代码的修改和变更,需要遵守[版权协议](./LICENSE)

# 6. 有用的链接
* [ISSUES](https://github.com/alipay/sofa-bolt/issues)
* [文档手册](https://github.com/alipay/sofa-bolt/wiki/SOFA-Bolt-Handbook)
* [中文介绍文章: 蚂蚁通信框架实践](http://mp.weixin.qq.com/s/JRsbK1Un2av9GKmJ8DK7IQ)