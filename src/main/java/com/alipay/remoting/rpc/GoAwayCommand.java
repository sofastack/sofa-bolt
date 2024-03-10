package com.alipay.remoting.rpc;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.util.IDGenerator;

public class GoAwayCommand extends RequestCommand {

    private static final long serialVersionUID = -5461716100952563423L;

    public GoAwayCommand() {
        super(CommonCommandCode.GOAWAY);
        this.setId(IDGenerator.nextId());
    }
}
