package com.alipay.remoting.simpledemo;

import java.io.Serializable;

public class SimpleRequest implements Serializable {

    private final int num;

    public SimpleRequest(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
