package com.imooc.factorybean;

/**
 * @author amos wong
 * @create 2020-07-26 3:58 下午
 */

public class FactoryBeanServiceImpl implements IFactoryBeanService {
    @Override
    public void sayHello() {
        System.out.println("hello factory bean service impl");
    }
}
