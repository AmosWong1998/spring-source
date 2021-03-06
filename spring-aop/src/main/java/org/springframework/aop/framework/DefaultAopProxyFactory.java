/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.framework;

import org.springframework.aop.SpringProxy;

import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * Default {@link AopProxyFactory} implementation, creating either a CGLIB proxy
 * or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true for a given
 * {@link AdvisedSupport} instance:
 * <ul>
 * <li>the {@code optimize} flag is set
 * <li>the {@code proxyTargetClass} flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>In general, specify {@code proxyTargetClass} to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {

	@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		/*
		 * 下面的三个条件简单分析一下：
		 *
		 *   条件1：config.isOptimize() - 是否需要优化，这个属性没怎么用过，
		 *         为true时：如果Bean有接口就直接使用JDK动态代理，没有接口就使用CGLIB
		 *   条件2：config.isProxyTargetClass() - 检测 proxyTargetClass 的值，
		 *         前面的代码会设置这个值
		 *   条件3：hasNoUserSuppliedProxyInterfaces(config)
		 *         - 目标 bean 是否实现了接口
		 * 当proxyTargetClass为true（前面设置的）时 就会优先使用CGLIB进行代理
		 *
		 * 如果被代理的目标类实现了一个或多个自定义的接口，那么就会使用 JDK 动态代理，
		 * 如果没有实现任何接口，会使用 CGLIB 实现代理，
		 * 如果设置了 proxy-target-class="true"，那么都会使用 CGLIB。
		 *
		 * JDK 动态代理基于接口，所以只有接口中的方法会被增强，
		 * 而 CGLIB 基于类继承，需要注意就是如果方法使用了 final 修饰，或者是 private 方法，是不能被增强的。
		 */
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			// 如果要代理的类本身就是接口
			// 或者它已经是个JDK的代理类（Proxy的子类，所有的JDK代理类都是此类的子类），
			// 也会用 JDK 动态代理
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			// 如果有接口，会跑到这个分支
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * 判断是否有实现自定义的接口
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class<?>[] ifcs = config.getProxiedInterfaces();
		return (ifcs.length == 0 || (ifcs.length == 1 && SpringProxy.class.isAssignableFrom(ifcs[0])));
	}

}
