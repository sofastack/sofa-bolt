package com.alipay.remoting.rpc;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.util.IDGenerator;

public class GoAwayCommand extends RequestCommand {

    //todo serialization

    public GoAwayCommand() {
        super(CommonCommandCode.GOAWAY);
        this.setId(IDGenerator.nextId());

    }
}
