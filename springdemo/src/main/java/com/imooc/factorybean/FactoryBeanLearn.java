package com.imooc.factorybean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author amos wong
 * @create 2020-07-26 3:57 下午
 */

@Component
public class FactoryBeanLearn implements FactoryBean<IFactoryBeanService> {
    @Override
    public IFactoryBeanService getObject() throws Exception {
        return new FactoryBeanServiceImpl();
    }

    @Override
    public Class<?> getObjectType() {
        return IFactoryBeanService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
