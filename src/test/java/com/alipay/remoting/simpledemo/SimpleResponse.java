package com.alipay.remoting.simpledemo;

import java.io.Serializable;

public class SimpleResponse implements Serializable {
    private int res;

    public SimpleResponse(int res) {
        this.res = res;
    }

    public int getRes() {
        return res;
    }
}
