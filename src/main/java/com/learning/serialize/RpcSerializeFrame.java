package com.learning.serialize;

import io.netty.channel.ChannelPipeline;

/**
 * RPC消息序列化选择器
 * @author Loring
 */
public interface RpcSerializeFrame {
    void select(RpcSerializeProtocol protocol, ChannelPipeline pipeline);
}
