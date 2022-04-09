# SOFABolt Project

[![Build Status](https://travis-ci.com/sofastack/sofa-bolt.svg?branch=master)](https://travis-ci.com/sofastack/sofa-bolt)
[![Coverage Status](https://codecov.io/gh/sofastack/sofa-bolt/branch/master/graph/badge.svg)](https://codecov.io/gh/sofastack/sofa-bolt)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![version](https://img.shields.io/maven-central/v/com.alipay.sofa/bolt.svg?label=bolt)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/sofastack/sofa-bolt.svg)](http://isitmaintained.com/project/sofastack/sofa-bolt "Percentage of issues still open")

# 1. 介绍
SOFABolt 是蚂蚁金融服务集团开发的一套基于 Netty 实现的网络通信框架。
* 为了让 Java 程序员能将更多的精力放在基于网络通信的业务逻辑实现上，而不是过多的纠结于网络底层 NIO 的实现以及处理难以调试的网络问题，Netty 应运而生。
* 为了让中间件开发者能将更多的精力放在产品功能特性实现上，而不是重复地一遍遍制造通信框架的轮子，SOFABolt 应运而生。

Bolt 名字取自迪士尼动画-闪电狗，是一个基于 Netty 最佳实践的轻量、易用、高性能、易扩展的通信框架。
这些年我们在微服务与消息中间件在网络通信上解决过很多问题，积累了很多经验，并持续的进行着优化和完善，我们希望能把总结出的解决方案沉淀到 SOFABolt 这个基础组件里，让更多的使用网络通信的场景能够统一受益。
目前该产品已经运用在了蚂蚁中间件的微服务 ([SOFARPC](https://github.com/sofastack/sofa-rpc))、消息中心、分布式事务、分布式开关、以及配置中心等众多产品上。

# 2. 功能介绍
![intro](.middleware-common/intro.png)

## SOFABolt　的基础功能包括：
* 基础通信功能 ( remoting-core )
    * 基于 Netty 高效的网络 IO 与线程模型运用
    * 连接管理 (无锁建连，定时断链，自动重连)
    * 基础通信模型 ( oneway，sync，future，callback )
    * 超时控制
    * 批量解包与批量提交处理器
    * 心跳与 IDLE 事件处理
* 协议框架 ( protocol-skeleton )
    * 命令与命令处理器
    * 编解码处理器
    * 心跳触发器
* 私有协议定制实现 - RPC 通信协议 ( protocol-implementation )
    * RPC 通信协议的设计
    * 灵活的反序列化时机控制
    * 请求处理超时 FailFast 机制
    * 用户请求处理器 ( UserProcessor )
    * 双工通信
    
## 用法1
将 SOFABolt 用作一个远程通信框架，使用者可以不用关心如何实现一个私有协议的细节，直接使用我们内置的 RPC 通信协议。可以非常简单的启动客户端与服务端，同时注册一个用户请求处理器，即可完成远程调用。同时，像连接管理、心跳等基础功能特性都默认可以使用。
当前支持的调用类型如下图所示：

![invoke_type](.middleware-common/invoke_types.png)

* 示例 Demo 请参考我们的 [用户手册](https://github.com/sofastack/sofa-bolt/wiki/SOFA-Bolt-Handbook#14-%E5%9F%BA%E7%A1%80%E9%80%9A%E4%BF%A1%E6%A8%A1%E5%9E%8B)

## 用法2
将 SOFABolt 用作一个协议框架，使用者可以复用基础的通信模型、协议包含的接口定义等基础功能。然后根据自己设计的私有协议自定义 Command 类型、Command 处理器、编解码处理器等。如下图所示，RPC 和消息的 Command 定义结构：

![msg_protocol](.middleware-common/msg_protocol.png)

# 4. 如何贡献
开放代码允许在签署协议之后，提交贡献代码。具体参考[如何参与贡献 SOFABolt 代码](./CONTRIBUTING.md)。

对 SOFABolt 代码的修改和变更，需要遵守[版权协议](./LICENSE)。

# 5. 多语言

* [node](https://github.com/sofastack/sofa-bolt-node)
* [python](https://github.com/sofastack/sofa-bolt-python)
* [cpp](https://github.com/sofastack/sofa-bolt-cpp)

# 6. 用户

<div>
<table>
    <tr>
      <td align="center" valign="middle">
			<img alt="蚂蚁集团" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*aK79TJUJykkAAAAAAAAAAAAAARQnAQ" height="60" />
      </td>
      <td align="center"  valign="middle">
        <img alt="网商银行" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*uAmFRZQ0Z4YAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
        <img alt="恒生电子" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*_iGLRq0Ih-IAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
        <img alt="数立信息" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*JAgIRpjz-IgAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="Paytm" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*a0fvTKJ1Xv8AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="天弘基金" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*99OQT7lDBsMAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="中国人保" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*P1BARJxwv1sAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="信美相互" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*jAzWQpIgFUAAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="南京银行" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*q9PMQI7hs8sAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="民生银行" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*GnUuSKmOtS4AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="重庆农商行" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*FKrxSYhdi2wAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="中信证券" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*t-xbQb3WSjcAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="富滇银行" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*JCDYT6u6_asAAAAAAAAAAAAAARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="挖财" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*lVrVT4dpSDEAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="拍拍贷" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*TAePS6j56LsAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="OPPO金融" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*mU40QaJkwZYAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="运满满" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*_kBbQYUYdIQAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="译筑科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*wuKSTpZSEkEAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="杭州米雅信息科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*b-o5SITMKu0AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="邦道科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*nsw1S5bt9DkAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="申通快递" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*C3ncSpDsjS8AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="深圳大头兄弟文化" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*8AYmRowxSC0AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="烽火科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*MjuuT4omCrwAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="亚信科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*vBBIRomYoEUAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="成都云智天下科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*p0OkQbC5RvsAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="上海溢米辅导" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*mJdtTJsn1PwAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="态赋科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*sfLDRL5TJx8AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="风一科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*EGeMR4qprnkAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="武汉易企盈" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*31WRQ7zg3HIAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="极致医疗" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*cPOiS5q8NCwAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="京东" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*INhuS44qO8YAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="小象生鲜" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*K5ERQYbCRBgAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="北京云族佳" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*qzxjSZ2tlmIAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="欣亿云网" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*huOKQKvoLzwAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="山东网聪" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*INUFR7XIH1gAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="深圳市诺安赛威" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*eVGbR7RhDDkAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="上扬软件" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*VsqMT7n7p0AAAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="长沙点三" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*2eEzSqdPIc0AAAAAAAAAAABjARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="网易云音乐" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*66KbQ6seDqoAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="虎牙直播" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*uzr3RLUZ3RwAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="中国移动" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*vEo-T55XTOAAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="无纸科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*9aFQSLfyPhMAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="黄金钱包" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*tYZJRpANxNoAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="独木桥网络" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*GW6oTLIlAbcAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="wueasy" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*4uFWQacI-RwAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="北京攸乐科技" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*CD5VT50FXqMAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="易宝支付" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*oy0ZSquXXjAAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="威马汽车" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*LPf2TbTeJPwAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
    </tr>
    <tr>
      <td align="center" valign="middle">
      <img alt="亿通国际" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*tlq4R7QqUaEAAAAAAAAAAABkARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="新华三" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*gw9uTbZvsbAAAAAAAAAAAAAAARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      <img alt="klilalagroup" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*0cskToqBSi8AAAAAAAAAAAAAARQnAQ" height="60" />
      </td>
      <td align="center" valign="middle">
      </td>
    </tr>
</table>
</div>

# 7. 联系我们

- 微信
  - 公众号：金融级分布式架构（Antfin_SOFA）：致力于打造一流的分布式技术在金融场景应用实践的技术交流平台，专注于交流金融科技行业内最前沿、可供参考的技术方案与实施路线。
    
    <img alt="Wechat" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*LVCnR6KtEfEAAAAAAAAAAABjARQnAQ" height="250" />
  
- 钉钉
  - 钉钉交流群：
    - ~~金融级分布式架构 SOFAStack 1群，群号：23127468~~ 已满
    - ~~金融级分布式架构 SOFAStack 2群，群号：23195297~~ 已满
    - ~~金融级分布式架构 SOFAStack 3群，群号：23390449~~ 已满
    - ~~金融级分布式架构 SOFAStack 4群，群号：23372465~~ 已满
    - ~~金融级分布式架构 SOFAStack 5群，群号：30315793~~ 已满
    - 金融级分布式架构 SOFAStack 6群，群号：34197075

      <img alt="DingTalk" src="https://gw.alipayobjects.com/mdn/sofastack/afts/img/A*SIFWTr5brrkAAAAAAAAAAAAAARQnAQ" height="250" />

  - 钉钉交流群：SOFAStack 金牌用户服务群，如果您已经在生产环境使用了 SOFAStack 相关组件，还请告知我们，我们将会邀请您加入到此群中，以便更加快捷的沟通和更加高效的线上使用问题支持。
