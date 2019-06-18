package com.learning.core.send;

import com.learning.serialize.RpcSerializeProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.util.Map;

public class MessageSendChannelInitializer extends ChannelInitializer {

    private RpcSerializeProtocol protocol;
    private RpcSendSerializeFrame frame = new RpcSendSerializeFrame();

    public MessageSendChannelInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol){
        this.protocol = protocol;
        return this;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        frame.select(protocol,pipeline);
    }

    //ObjectDecoder 底层默认继承半包解码器LengthFieldBasedFrameDecoder处理粘包问题的时候，
    //消息头开始即为长度字段，占据4个字节。这里出于保持兼容的考虑
//    final public static int MESSAGE_LENGTH = 4;
//
//    @Override
//    protected void initChannel(Channel channel) throws Exception {
//        ChannelPipeline pipeline = channel.pipeline();
//
//        //ObjectDecoder的基类半包解码器LengthFieldBasedFrameDecoder的报文格式保持兼容。因为底层的父类LengthFieldBasedFrameDecoder
//        //的初始化参数即为super(maxObjectSize, 0, 4, 0, 4);
//        // 拆包的 InBound
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, MessageSendChannelInitializer.MESSAGE_LENGTH, 0, MessageSendChannelInitializer.MESSAGE_LENGTH));
//        //利用LengthFieldPrepender回填补充ObjectDecoder消息报文头
//        //考虑到并发性能，采用weakCachingConcurrentResolver缓存策略。一般情况使用:cacheDisabled即可
//        pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
//        pipeline.addLast(new MessageSendHandler());
//
//        // OutBound
//        // 装包的
//        pipeline.addLast(new LengthFieldPrepender(MessageSendChannelInitializer.MESSAGE_LENGTH));
//        pipeline.addLast(new ObjectEncoder());
//    }
}
