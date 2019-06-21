package com.learning.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class LightRpcNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service",new LightRpcServiceParser());
        registerBeanDefinitionParser("registry",new LightRpcRegistryParser());
    }
}
