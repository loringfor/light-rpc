package com.learning.spring;

import com.learning.core.recv.MessageRecvExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author loring
 * @date 2019.6.21
 * 自定义Spring标签的service的pojo类
 */
public class LightRpcService implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(LightRpcService.class);
    private String interfaceName;
    private String ref;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Object obj = applicationContext.getBean(ref);
        MessageRecvExecutor.getInstance().getConcurrentHashMap().put(interfaceName,obj);
        logger.info("Service put in HashMap, >>>key:{},>>>value:{}", interfaceName, obj.toString());
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
