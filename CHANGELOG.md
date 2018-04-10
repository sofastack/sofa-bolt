## Nouns
* **BC**: backward compatibility. It means this version is compatible with the previous version.
* **NonBC**: non-backward compatibility. It means this version is NOT compatible with the previous version.
* **RCMD**: recommended. It means this version is mostly used, stable and recommended.
* **NotRCMD**: not recommended. It means this version is defective and problematic, not recommended.
* **LINKE/TGT**: This is the origin url of R&D process management, where you can find the whole develop process for this version.

## [bolt-1.3.1 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/22)
 * **RCMD**|**BC**|[LINKE](http://e.alipay.net/#/alipay/iteration/EI11009083/bolt?_k=r4zgma)
 * changes:
   * feature: support to set netty write buffer high water mark and low water mark, high watermark should not be smaller than low watermark 
      * usage1: setting by jvm args `-Dbolt.netty.buffer.high.watermark=65536 -Dbolt.netty.buffer.low.watermark=32768`
      * usage2: setting by system property `System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(128 * 1024));System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(32 * 1024));`
   * log tuning: print exception class name when exceptionCaught of ConnectionEventHandler.   
 * upgrade guide: 
   * this is a special version, the watermark-setting feature only support in 1.1.7 and 1.2.4 and 1.3.1
      * if you use 1.1.x now, upgrade to >=1.1.7 to acquire this feature
      * if you use 1.2.x now, upgrade to >=1.2.4 to acquire this feature
      * if you use 1.3.x now, upgrade to >=1.3.1 to acquire this feature

## [bolt 1.3.0 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/20)
* **NonBC**|[LINKE](http://e.alipay.net/#/alipay/iteration/EI11003211/bolt)
* changes:
  * features:  
      * support crc32 check in remoting protocol, client can enable or disable this feature by specify crc32 switch
      * print log in asynchronous way
      * add apis to get `timeout` and `arriveTimestamp` in `BizContext`
      * support shutdown `RemotingServer` in sync way
  * bugfix:
      * tuning log and fix NPE problems
* upgrade guide: this version is not compatible mainly because of [asynchronous log](http://gitlab.alipay-inc.com/alipay-com/bolt/issues/189) and [crc feature](http://gitlab.alipay-inc.com/alipay-com/bolt/issues/165), you should see the two issue carefully and then upgrade.

## [bolt-1.2.4 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones)
 * **RCMD**|**BC**|[LINKE](http://e.alipay.net/#/alipay/iteration/EI11009080/bolt?_k=iwbba4)
 * changes:
   * feature: support to set netty write buffer high water mark and low water mark, high watermark should not be smaller than low watermark
      * usage1: setting by jvm args `-Dbolt.netty.buffer.high.watermark=65536 -Dbolt.netty.buffer.low.watermark=32768`
      * usage2: setting by system property `System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(128 * 1024));System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(32 * 1024));`
 * upgrade guide: 
   * this is a special version, the watermark-setting feature only support in 1.1.7 and 1.2.4 and 1.3.1
      * if you use 1.1.x now, upgrade to >=1.1.7 to acquire this feature
      * if you use 1.2.x now, upgrade to >=1.2.4 to acquire this feature
      * if you use 1.3.x now, upgrade to >=1.3.1 to acquire this feature
             
## [bolt 1.2.3 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/21)
* **RCMD**|**BC**|[LINKE](http://linke.antcloud.alipay.net/index.html#/rnd/flow/59de02be7b92ce2274697a88?_k=h1ou5q)
* changes:
  * features:  
      * add get connection function in user processor
  * bugfix:
      * timeout settings not work for getConnection api
  * upgrade guide: totally compatible with 1.2.2, you can just modify version number.
      
## [bolt 1.2.2 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/19)
* **RCMD**|**NonBC**|[LINKE](http://linke.antcloud.alipay.net/index.html#/rnd/flow/59ae68917b92ce7a6229cd97?_k=3wig6u)
* changes:
  * features:  
      * integrate with sofa middleware log and supply default log conf.
  * tunings:
      * fix too many "Connection reset by peer" exceptions when exceptionCaught
  * upgrade guide: You can delete the appender/logger conf for bolt if you defined one once, bolt can print logs by itself now. Detail upgrade introduction, please check issue [bolt 1.2.2 release guide](http://gitlab.alipay-inc.com/alipay-com/bolt/issues/182)
      
## [bolt 1.2.1 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/18)
* **BC**|[LINKE]()
* changes:
  * tuning some log issues and fix an NPE problem, see [issue](http://gitlab.alipay-inc.com/alipay-com/bolt/issues/178) for detail.
* upgrade guide: totally compatible with 1.2.0
* NOTE: this version is only for public ant cloud.
  
## [bolt 1.2.0 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/16)
* **NonBC**|[LINKE](http://linke.antcloud.alipay.net/index.html#/rnd/flow/5965d00b7b92ce45312eca5a?_k=qtum4z)
* changes:
  * features: 
      * Non-lock style implementation for ConnectionManager when create connections
      * suply a switch to handle request timeout in IO thread
  * tunings:  
      * tuning the trigger mechanism of connection event 
      * add license for all source code
      * remove dependency of log4j, replaced with slf4j
  * upgrade guide: This version is not compatible because of `InterruptedException` should be handled when do invoke, and some package path for utils modified. Detail upgrade introduction, please check issue [bolt 1.2.0 release guide](http://gitlab.alipay-inc.com/alipay-com/bolt/issues/172)

## [bolt-1.1.7 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/22)
  * **RCMD**|**BC**|[LINKE](http://e.alipay.net/#/alipay/iteration/EI11009059/bolt?_k=uoqlvj)
  * changes:
    * feature: support to set netty write buffer high water mark and low water mark, high watermark should not be smaller than low watermark
       * usage1: setting by jvm args `-Dbolt.netty.buffer.high.watermark=65536 -Dbolt.netty.buffer.low.watermark=32768`
       * usage2: setting by system property `System.setProperty(Configs.NETTY_BUFFER_HIGH_WATERMARK, Integer.toString(128 * 1024));System.setProperty(Configs.NETTY_BUFFER_LOW_WATERMARK, Integer.toString(32 * 1024));`
  * upgrade guide: 
    * this is a special version, the watermark-setting feature only support in 1.1.7 and 1.2.4 and 1.3.1
       * if you use 1.1.x now, upgrade to >=1.1.7 to acquire this feature
       * if you use 1.2.x now, upgrade to >=1.2.4 to acquire this feature
       * if you use 1.3.x now, upgrade to >=1.3.1 to acquire this feature
 
## [bolt 1.1.6 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/14)
 * **RCMD**|**BC**|[LINKE](http://linke.antcloud.alipay.net/index.html#/rnd/flow/58e4636c7b92ce1a26a4d07c?_k=vew1x8)
 * changes:
   * feature: add codec field in InvokeContext to support custom codec for each request.
   * tuning: Refine the logic of choosing which thread pool to process request.
   * products related:
     * ce4 series: ce4.4.2.0

## [bolt 1.1.5 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/12)
 * **BC**|[LINKE](http://linke.antcloud.alipay.net/index.html#/rnd/flow/58c604087b92ce59ab996ad2?_k=btwbkd)
 * changes:
   * feature: switch for connection monitor and switch for reconnection
   * products related:
     * ce3 series: ce3.4.3.7
     * ce4 series: ce4.4.1.1
     
## [bolt 1.1.4 release]()
 * **BC**
 * changes:
   * change reconnect error log to warn

## [bolt 1.1.3 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/10)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/18601)
 * changes:
   * bugfix: IMPORTANT! fix dead lock problems during creating connections.
   * products related:
     * ce3 series: ce3.4.2.2
     * ce4 series: ce4.3.0.1

## [bolt 1.1.2 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/9)
 * **NotRCMD**|**BC**|[TGT](http://together.alipay.net/#/projectDetail/18430)
 * changes:
   * bugfix: IMPORTANT! Hot-spot lock during creating connections, leading to requests queued when do concurrent invoke by RpcClient. detail see: alipay-com/bolt#113
   * feature: define a new key CLIENT_CONN_CREATETIME of invokecontext, to provide the time consumed value when create connection.
   * products related:
     * ce3 series: ce3.4.2.1

## [bolt 1.1.1 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/8)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/17742)
 * changes:
   * bugfix: poolKey of Connection dispatched by ConnectionEventHandler maybe null, fix this.
   * bugfix: prevent invoke InvokeCallback#onException() when Exception occurred in InvokeCallback#onResponse() methods.
 * products related:
   * ce3 series: ce3.4.1.3
   * ce4 series: ce4.3.0.0

## [bolt 1.1.0 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/6)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/16920)
 * changes:
   * feature: support invoke context mechanism, user can get more infomation from bolt.
   * bugfix: tune connection event dispatcher mechanism
   * bugfix: NPE when decode illegal byte and NPE when execute task of DefaultInvokeFuture
   * tuning: tune logs
 * products related:
   * ce3 series: ce3.4.1.1, ce3.4.1.2

## [bolt 1.0.2.1 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/7)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/16738)
 * changes:
   * maily support classload switch when do deserialize response in call back listener
 * products related:
   * ce3 series: ce3.4.1.0
 
## [bolt 1.0.2 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/5)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/14272)
 * changes:
   * provide an async way {@link AsyncUserProcessor} to process request
   * remove while statement to prevent infinite loop in ConnectionEventHandler. 
   * **PAY ATTENTION!!!** if you strongly depend on the connection event dispatcher mechanism, please don't use this version.
   * We will fix this problem in the following versions.
 * products related:
   * no ce version used this release

## [bolt 1.0.1 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/4)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/13924)
 * changes:
   * tuning ProtocolDecode#channelRead
   * close all connections when stop RpcClient and RpcServer. For multi-tenant concern, netty workGroup is set to be static, in order to share between threads. If you want to stop RpcClient or RpcServer at runtime, please use this version)
 * products related:
   * ce3 series: ce3.4.0.1, ce3.4.0.2, ce3.4.0.3
   * ce4 series: ce4.2.0.0, ce4.2.0.1
 
## [bolt 1.0.0 release](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/3)
 * **BC**|[TGT](http://together.alipay.net/#/projectDetail/13338)
 * changes:
   * release 1.0.0
 * products related:
   * maily used in ce3.4.0.0

## [bolt 1.0.0-SANPSHOT](http://gitlab.alipay-inc.com/alipay-com/bolt/milestones/2)
 * release 1.0.0-SANPSHOT