package com.learning.spring;

import com.learning.core.recv.MessageRecvExecutor;
import com.learning.registry.ServiceRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LightRpcRegistry implements ApplicationContextAware {
    private String protocol;
    private String address;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
        MessageRecvExecutor messageRecvExecutor = MessageRecvExecutor.getInstance();
        System.out.println(messageRecvExecutor.getConcurrentHashMap().size());
        ServiceRegistry serviceRegistry = new ServiceRegistry(address);
        messageRecvExecutor.setServiceRegistry(serviceRegistry);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
