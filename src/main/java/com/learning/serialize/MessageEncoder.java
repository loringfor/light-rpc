package com.learning.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC消息编码接口
 * @author Loring
 */
public class MessageEncoder extends MessageToByteEncoder<Object> {
    private MessageCodecUtil util;

    public MessageEncoder(MessageCodecUtil util) {
        this.util = util;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        util.encode(out, msg);
    }
}
