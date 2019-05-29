package com.learning.core.recv;

import com.learning.model.MessageResponse;
import com.learning.model.MessageRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.reflect.MethodUtils;
import java.util.Map;

public class MessageRecvInitializeTask implements Runnable {

    private MessageRequest request;
    private MessageResponse reponse;
    private Map<String,Object> concurrentMap;
    private ChannelHandlerContext context;

    public MessageRecvInitializeTask(MessageRequest request, MessageResponse reponse, Map<String, Object> concurrentMap, ChannelHandlerContext context) {
        this.request = request;
        this.reponse = reponse;
        this.concurrentMap = concurrentMap;
        this.context = context;
    }

    @Override
    public void run() {
        reponse.setMessageId(request.getMessageId());
        try{
            Object result = reflect(request);
            reponse.setResultDesc(result);
        } catch (Throwable throwable) {
            System.err.printf("RPC Server invoke error!\n");
            throwable.printStackTrace();
        }
        context.writeAndFlush(reponse).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.println("RPC Server Send message-id respone:" + request.getMessageId());
            }
        });

    }

    public Object reflect(MessageRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = concurrentMap.get(className);
        String methodName = request.getMethodName();
        Object[] parameters = request.getParametersVal();
        return MethodUtils.invokeMethod(serviceBean, methodName, parameters);
    }

    public MessageRequest getRequest() {
        return request;
    }

    public void setRequest(MessageRequest request) {
        this.request = request;
    }

    public MessageResponse getReponse() {
        return reponse;
    }

    public void setReponse(MessageResponse reponse) {
        this.reponse = reponse;
    }

    public Map<String, Object> getConcurrentMap() {
        return concurrentMap;
    }

    public void setConcurrentMap(Map<String, Object> concurrentMap) {
        this.concurrentMap = concurrentMap;
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }
}
