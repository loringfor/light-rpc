package com.learning.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {

    final public static int MESSAGE_LENGTH = MessageCodecUtil.MESSAGE_LENGTH;
    private MessageCodecUtil util;

    public MessageDecoder(MessageCodecUtil util) {
        this.util = util;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //出现粘包导致消息头长度不对，直接返回
        if(in.readableBytes()<MessageDecoder.MESSAGE_LENGTH)
            return;
        in.markReaderIndex();

        // 读取消息的内容长度
        int messageLength = in.readInt();

        if(messageLength < 0){
            ctx.close();
        }

        //读到的消息长度和报文头的已知长度不匹配。那就重置一下ByteBuf读索引的位置
        if (in.readableBytes()<messageLength){
            in.resetReaderIndex();
            return;
        }else {
            byte[] messageBody = new byte[messageLength];
            in.readBytes(messageBody);
            Object object = util.decode(messageBody);
            out.add(object);
        }
    }
}
