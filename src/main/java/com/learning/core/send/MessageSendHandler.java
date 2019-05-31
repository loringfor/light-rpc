package com.learning.core.send;

import com.learning.core.MessageCallBack;
import com.learning.model.MessageResponse;
import com.learning.model.MessageRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MessageSendHandler.class);
    private ConcurrentHashMap<String, MessageCallBack> mapCallBack = new ConcurrentHashMap();
    private volatile Channel channel;
    private SocketAddress remoteAddr;

    public Channel getChannel() {
        return channel;
    }
    public SocketAddress getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageResponse messageResponse =(MessageResponse)msg;
        String messageId = messageResponse.getMessageId();
        MessageCallBack callBack = mapCallBack.get(messageId);
        if(callBack!=null){
            mapCallBack.remove(messageId);
            callBack.over(messageResponse);
        }
    }

    // 先注册
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel=ctx.channel();
    }

    // 后连接启动
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("建立了连接：{}", channel.remoteAddress().toString());
        this.remoteAddr=this.channel.remoteAddress();
    }

    public MessageCallBack sendRequest(MessageRequest request){
        MessageCallBack messageCallBack =new MessageCallBack(request);
        // 建立requestId与callBack之间的关系
        mapCallBack.put(request.getMessageId(),messageCallBack);
        channel.writeAndFlush(request);
        return messageCallBack;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

}
