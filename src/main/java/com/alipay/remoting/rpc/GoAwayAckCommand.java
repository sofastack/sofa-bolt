package com.alipay.remoting.rpc;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.util.IDGenerator;

public class GoAwayAckCommand extends ResponseCommand {

    //todo serialization

    public GoAwayAckCommand() {
        super(CommonCommandCode.GOAWAY);
        this.setResponseStatus(ResponseStatus.SUCCESS);
    }
}
