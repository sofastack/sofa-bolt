package com.alipay.remoting;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-11-05 14:27
 */
public interface LifeCycle {

    void startup() throws LifeCycleException;

    void shutdown() throws LifeCycleException;

    boolean isStarted();
}
