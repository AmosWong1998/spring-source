package com.imooc.postprocessor;

import com.imooc.entity.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomizedBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		Class<?> clazz = User.class;
		//BeanDefinitionBuilder工具类, 用以创建GenericBeanDefinition实例
		BeanDefinitionBuilder builder =BeanDefinitionBuilder.genericBeanDefinition(clazz);
		//获取User对应的GenericBeanDefinition实例
		GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
		// 将其注册到容器中
		registry.registerBeanDefinition("user5", definition);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}
}
