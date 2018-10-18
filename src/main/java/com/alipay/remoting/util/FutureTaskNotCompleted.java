package com.alipay.remoting.util;

/**
 * @author chengyi (mark.lx@antfin.com) 2018-10-18 16:30
 */
public class FutureTaskNotCompleted extends Exception {

    private static final long serialVersionUID = -3635466558774380138L;

    public FutureTaskNotCompleted() {
    }

    public FutureTaskNotCompleted(String message) {
        super(message);
    }

    public FutureTaskNotCompleted(String message, Throwable cause) {
        super(message, cause);
    }
}
