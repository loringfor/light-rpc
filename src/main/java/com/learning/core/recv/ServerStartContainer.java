package com.learning.core.recv;

import com.learning.serialize.RpcSerializeProtocol;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ServerStartContainer implements ApplicationContextAware {
    private String ipAddress;
    private String weight;
    private RpcSerializeProtocol protocol;
    private ApplicationContext applicationContext;

    public ServerStartContainer() {
    }

    public ServerStartContainer(String ipAddress, String weight, RpcSerializeProtocol protocol) {
        this.ipAddress = ipAddress;
        this.weight = weight;
        this.protocol = protocol;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
        MessageRecvExecutor messageRecvExecutor = MessageRecvExecutor.getInstance();
        System.out.println("hashMap size: "+messageRecvExecutor.getConcurrentHashMap().size());
        messageRecvExecutor.setServerAddress(ipAddress);
        messageRecvExecutor.setServerWeight(weight);
        messageRecvExecutor.setSerializeProtocol(protocol);
        try {
            messageRecvExecutor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public RpcSerializeProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(RpcSerializeProtocol protocol) {
        this.protocol = protocol;
    }
}
