package com.learning.core.recv;

import com.learning.model.MessageResponse;
import com.learning.model.MessageRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.Map;

public class MessageRecvHandler extends ChannelInboundHandlerAdapter {
    private Map<String, Object> concurrentMap;

    public MessageRecvHandler(Map<String, Object> concurrentMap) {
        this.concurrentMap = concurrentMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageRequest request = (MessageRequest)msg;
        MessageResponse reponse = new MessageResponse();
        MessageRecvInitializeTask recvTask = new MessageRecvInitializeTask(request,reponse,concurrentMap,ctx);
        //不要阻塞nio线程，复杂的业务逻辑丢给专门的线程池
        MessageRecvExecutor.submit(recvTask);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端收到建立连接了！");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
