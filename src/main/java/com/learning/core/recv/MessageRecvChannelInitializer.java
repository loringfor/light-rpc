package com.learning.core.recv;

import com.learning.serialize.RpcSerializeProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.util.Map;

public class MessageRecvChannelInitializer extends ChannelInitializer {
    private RpcSerializeProtocol protocol;
    private RpcRecvSerializeFrame frame;

    MessageRecvChannelInitializer buildRpcSerializeProtocol(RpcSerializeProtocol protocol){
        this.protocol=protocol;
        return this;
    }

    MessageRecvChannelInitializer(Map<String,Object> concurrentMap){
        frame = new RpcRecvSerializeFrame(concurrentMap);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 根据不同的协议来进行序列化/反序列化和编解码
        frame.select(protocol,pipeline);
    }

    //ObjectDecoder 底层默认继承半包解码器LengthFieldBasedFrameDecoder处理粘包问题的时候，
    //消息头开始即为长度字段，占据4个字节。这里出于保持兼容的考虑
//    final public static int MESSAGE_LENGTH = 4;
//    private Map<String, Object> concurrentMap = null;
//
//    public MessageRecvChannelInitializer(Map<String, Object> concurrentMap) {
//        this.concurrentMap = concurrentMap;
//    }
//
//    @Override
//    protected void initChannel(Channel channel) throws Exception {
//        ChannelPipeline pipeline = channel.pipeline();
//
//        // InBound
//        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0, MessageRecvChannelInitializer.MESSAGE_LENGTH, 0, MessageRecvChannelInitializer.MESSAGE_LENGTH));
//        pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
//
//        // OutBound
//        pipeline.addLast(new LengthFieldPrepender(MessageRecvChannelInitializer.MESSAGE_LENGTH));
//        pipeline.addLast(new ObjectEncoder());
//
//        pipeline.addLast(new MessageRecvHandler(concurrentMap));
//    }
}
