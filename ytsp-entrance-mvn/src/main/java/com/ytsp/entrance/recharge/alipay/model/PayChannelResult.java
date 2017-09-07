package com.ytsp.entrance.recharge.alipay.model;

import java.util.List;

public class PayChannelResult {

    private LastestPayChannel lastestPayChannel;
    private List<SupportTopPayChannel> supportedPayChannelList;

    public LastestPayChannel getLastestPayChannel() {
        return lastestPayChannel;
    }

    public void setLastestPayChannel(LastestPayChannel lastestPayChannel) {
        this.lastestPayChannel = lastestPayChannel;
    }

    public List<SupportTopPayChannel> getSupportedPayChannelList() {
        return supportedPayChannelList;
    }

    public void setSupportedPayChannelList(List<SupportTopPayChannel> supportedPayChannelList) {
        this.supportedPayChannelList = supportedPayChannelList;
    }

}
