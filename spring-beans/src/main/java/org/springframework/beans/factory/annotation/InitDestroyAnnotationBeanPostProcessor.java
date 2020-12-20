/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} implementation
 * that invokes annotated init and destroy methods. Allows for an annotation
 * alternative to Spring's {@link org.springframework.beans.factory.InitializingBean}
 * and {@link org.springframework.beans.factory.DisposableBean} callback interfaces.
 *
 * <p>The actual annotation types that this post-processor checks for can be
 * configured through the {@link #setInitAnnotationType "initAnnotationType"}
 * and {@link #setDestroyAnnotationType "destroyAnnotationType"} properties.
 * Any custom annotation can be used, since there are no required annotation
 * attributes.
 *
 * <p>Init and destroy annotations may be applied to methods of any visibility:
 * public, package-protected, protected, or private. Multiple such methods
 * may be annotated, but it is recommended to only annotate one single
 * init method and destroy method, respectively.
 *
 * <p>Spring's {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor}
 * supports the JSR-250 {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy}
 * annotations out of the box, as init annotation and destroy annotation, respectively.
 * Furthermore, it also supports the {@link javax.annotation.Resource} annotation
 * for annotation-driven injection of named beans.
 *
 * @author Juergen Hoeller
 * @see #setInitAnnotationType
 * @see #setDestroyAnnotationType
 * @since 2.5
 */
@SuppressWarnings("serial")
public class InitDestroyAnnotationBeanPostProcessor
        implements DestructionAwareBeanPostProcessor, MergedBeanDefinitionPostProcessor, PriorityOrdered, Serializable {

    private final transient LifecycleMetadata emptyLifecycleMetadata =
            new LifecycleMetadata(Object.class, Collections.emptyList(), Collections.emptyList()) {
                @Override
                public void checkConfigMembers(RootBeanDefinition beanDefinition) {
                }

                @Override
                public void invokeInitMethods(Object target, String beanName) {
                }

                @Override
                public void invokeDestroyMethods(Object target, String beanName) {
                }

                @Override
                public boolean hasDestroyMethods() {
                    return false;
                }
            };


    protected transient Log logger = LogFactory.getLog(getClass());

    @Nullable
    private Class<? extends Annotation> initAnnotationType;

    @Nullable
    private Class<? extends Annotation> destroyAnnotationType;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Nullable
    private final transient Map<Class<?>, LifecycleMetadata> lifecycleMetadataCache = new ConcurrentHashMap<>(256);


    /**
     * Specify the init annotation to check for, indicating initialization
     * methods to call after configuration of a bean.
     * <p>Any custom annotation can be used, since there are no required
     * annotation attributes. There is no default, although a typical choice
     * is the JSR-250 {@link javax.annotation.PostConstruct} annotation.
     */
    public void setInitAnnotationType(Class<? extends Annotation> initAnnotationType) {
        this.initAnnotationType = initAnnotationType;
    }

    /**
     * Specify the destroy annotation to check for, indicating destruction
     * methods to call when the context is shutting down.
     * <p>Any custom annotation can be used, since there are no required
     * annotation attributes. There is no default, although a typical choice
     * is the JSR-250 {@link javax.annotation.PreDestroy} annotation.
     */
    public void setDestroyAnnotationType(Class<? extends Annotation> destroyAnnotationType) {
        this.destroyAnnotationType = destroyAnnotationType;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return this.order;
    }


    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        // 寻找生命周期元数据
        LifecycleMetadata metadata = findLifecycleMetadata(beanType);
        // 对收集到的声明周期方法做一下校验处理
        metadata.checkConfigMembers(beanDefinition);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeInitMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
        try {
            metadata.invokeDestroyMethods(bean, beanName);
        } catch (InvocationTargetException ex) {
            String msg = "Destroy method on bean with name '" + beanName + "' threw an exception";
            if (logger.isDebugEnabled()) {
                logger.warn(msg, ex.getTargetException());
            } else {
                logger.warn(msg + ": " + ex.getTargetException());
            }
        } catch (Throwable ex) {
            logger.warn("Failed to invoke destroy method on bean with name '" + beanName + "'", ex);
        }
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return findLifecycleMetadata(bean.getClass()).hasDestroyMethods();
    }


    private LifecycleMetadata findLifecycleMetadata(Class<?> clazz) {
        if (this.lifecycleMetadataCache == null) {
            // Happens after deserialization, during destruction...
            // 没有开启缓存就直接拿构建生命周期元数据了
            return buildLifecycleMetadata(clazz);
        }
        // Quick check on the concurrent map first, with minimal locking.
        // 有开启缓存的话，就先从缓存找，找不到再构建，然后丢回缓存
        LifecycleMetadata metadata = this.lifecycleMetadataCache.get(clazz);
        if (metadata == null) {
            synchronized (this.lifecycleMetadataCache) {
                metadata = this.lifecycleMetadataCache.get(clazz);
                if (metadata == null) {
                    metadata = buildLifecycleMetadata(clazz);
                    this.lifecycleMetadataCache.put(clazz, metadata);
                }
                return metadata;
            }
        }
        return metadata;
    }

    private LifecycleMetadata buildLifecycleMetadata(final Class<?> clazz) {
        // 简单判断类上是不是一定没有initAnnotationType和destroyAnnotationType这两个注解修饰的方法
        // 相当于快速失败
        // 需要注意的是，当前场景下，这两个注解实例化的时候已经初始化为PostConstruct和PreDestroy了
        if (!AnnotationUtils.isCandidateClass(clazz, Arrays.asList(this.initAnnotationType, this.destroyAnnotationType))) {
            return this.emptyLifecycleMetadata;
        }

        // 用来储存类上所有初始化/销毁方法的容器
        List<LifecycleElement> initMethods = new ArrayList<>();
        List<LifecycleElement> destroyMethods = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            // 中间容器来储存当前类的初始化/销毁方法
            final List<LifecycleElement> currInitMethods = new ArrayList<>();
            final List<LifecycleElement> currDestroyMethods = new ArrayList<>();

            // 循环类上的每一个方法
            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                if (this.initAnnotationType != null && method.isAnnotationPresent(this.initAnnotationType)) {
                    // 如果方法被@PostConstruct注解修饰，包装成一个LifecycleElement
                    LifecycleElement element = new LifecycleElement(method);
                    // 加入收集初始化方法的中间容器
                    currInitMethods.add(element);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Found init method on class [" + clazz.getName() + "]: " + method);
                    }
                }
                if (this.destroyAnnotationType != null && method.isAnnotationPresent(this.destroyAnnotationType)) {
                    // 如果方法被@PreDestroy注解修饰，包装成一个LifecycleElement
                    // 加入收集销毁方法的中间容器
                    currDestroyMethods.add(new LifecycleElement(method));
                    if (logger.isTraceEnabled()) {
                        logger.trace("Found destroy method on class [" + clazz.getName() + "]: " + method);
                    }
                }
            });

            // 加入所有初始化/销毁方法的容器
            // 需要注意的是，在整个循环过程中，
            // 当前类的初始化方法都是加入初始化方法容器的头部
            // 当前类的销毁方法都是加入销毁方法容器的尾部
            // 所以可以推断，初始化方法调用的时候是从父类->子类调用
            // 而销毁方法从子类->父类调用。
            // 即 bean初始化->调用父类初始化方法->调用子类初始化方法->...->调用子类销毁方法->调用父类销毁方法->销毁bean
            initMethods.addAll(0, currInitMethods);
            destroyMethods.addAll(currDestroyMethods);
            // 获取父类，循环处理所有父类上的初始化/销毁方法
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        // 把当前类class对象+初始化方法列表+销毁方法列表封装成一个LifecycleMetadata对象
        return (initMethods.isEmpty() && destroyMethods.isEmpty() ? this.emptyLifecycleMetadata :
                new LifecycleMetadata(clazz, initMethods, destroyMethods));
    }


    //---------------------------------------------------------------------
    // Serialization support
    //---------------------------------------------------------------------

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Rely on default serialization; just initialize state after deserialization.
        ois.defaultReadObject();

        // Initialize transient fields.
        this.logger = LogFactory.getLog(getClass());
    }


    /**
     * Class representing information about annotated init and destroy methods.
     */
    private class LifecycleMetadata {

        // 目标类
        private final Class<?> targetClass;

        // 目标类上的初始化方法
        private final Collection<LifecycleElement> initMethods;

        // 目标类上的销毁方法
        private final Collection<LifecycleElement> destroyMethods;

        // 检查、校验后的初始化方法
        @Nullable
        private volatile Set<LifecycleElement> checkedInitMethods;

        // 检查、校验后的销毁方法
        @Nullable
        private volatile Set<LifecycleElement> checkedDestroyMethods;

        public LifecycleMetadata(Class<?> targetClass, Collection<LifecycleElement> initMethods,
                                 Collection<LifecycleElement> destroyMethods) {

            this.targetClass = targetClass;
            this.initMethods = initMethods;
            this.destroyMethods = destroyMethods;
        }

        public void checkConfigMembers(RootBeanDefinition beanDefinition) {
            // 这是那个检查、校验方法
            Set<LifecycleElement> checkedInitMethods = new LinkedHashSet<>(this.initMethods.size());
            // 循环处理每个初始化方法
            for (LifecycleElement element : this.initMethods) {
                String methodIdentifier = element.getIdentifier();
                // 判断是否是标记为外部处理的初始化方法，
                // 如果是外部处理的方法的话，其实spring是不会管理这些方法的
                if (!beanDefinition.isExternallyManagedInitMethod(methodIdentifier)) {
                    // 这里把当前方法注册到这个externallyManagedDestroyMethods
                    // 我猜想是方法签名相同的方法就不调用两次了
                    // 比如可能父类的方法打了@PostConstruct,子类重写之后也在方法上打了@PostConstruct
                    // 这两个方法都会被收集到initMethods，但是当然不应该调用多次
                    beanDefinition.registerExternallyManagedInitMethod(methodIdentifier);
                    // 加入了检查后的初始化方法列表，实际调用初始化方法时也是会调用这个列表
                    checkedInitMethods.add(element);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Registered init method on class [" + this.targetClass.getName() + "]: " + element);
                    }
                }
            }
            Set<LifecycleElement> checkedDestroyMethods = new LinkedHashSet<>(this.destroyMethods.size());
            for (LifecycleElement element : this.destroyMethods) {
                String methodIdentifier = element.getIdentifier();
                if (!beanDefinition.isExternallyManagedDestroyMethod(methodIdentifier)) {
                    beanDefinition.registerExternallyManagedDestroyMethod(methodIdentifier);
                    checkedDestroyMethods.add(element);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Registered destroy method on class [" + this.targetClass.getName() + "]: " + element);
                    }
                }
            }
            // 销毁方法的处理逻辑和初始化方法一样，我直接跳过了
            this.checkedInitMethods = checkedInitMethods;
            this.checkedDestroyMethods = checkedDestroyMethods;
        }

        public void invokeInitMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleElement> checkedInitMethods = this.checkedInitMethods;
            Collection<LifecycleElement> initMethodsToIterate =
                    (checkedInitMethods != null ? checkedInitMethods : this.initMethods);
            if (!initMethodsToIterate.isEmpty()) {
                for (LifecycleElement element : initMethodsToIterate) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Invoking init method on bean '" + beanName + "': " + element.getMethod());
                    }
                    element.invoke(target);
                }
            }
        }

        public void invokeDestroyMethods(Object target, String beanName) throws Throwable {
            Collection<LifecycleElement> checkedDestroyMethods = this.checkedDestroyMethods;
            Collection<LifecycleElement> destroyMethodsToUse =
                    (checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
            if (!destroyMethodsToUse.isEmpty()) {
                for (LifecycleElement element : destroyMethodsToUse) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Invoking destroy method on bean '" + beanName + "': " + element.getMethod());
                    }
                    element.invoke(target);
                }
            }
        }

        public boolean hasDestroyMethods() {
            Collection<LifecycleElement> checkedDestroyMethods = this.checkedDestroyMethods;
            Collection<LifecycleElement> destroyMethodsToUse =
                    (checkedDestroyMethods != null ? checkedDestroyMethods : this.destroyMethods);
            return !destroyMethodsToUse.isEmpty();
        }
    }


    /**
     * Class representing injection information about an annotated method.
     */
    private static class LifecycleElement {

        private final Method method;

        private final String identifier;

        public LifecycleElement(Method method) {
            if (method.getParameterCount() != 0) {
                throw new IllegalStateException("Lifecycle method annotation requires a no-arg method: " + method);
            }
            this.method = method;
            this.identifier = (Modifier.isPrivate(method.getModifiers()) ?
                    ClassUtils.getQualifiedMethodName(method) : method.getName());
        }

        public Method getMethod() {
            return this.method;
        }

        public String getIdentifier() {
            return this.identifier;
        }

        public void invoke(Object target) throws Throwable {
            ReflectionUtils.makeAccessible(this.method);
            this.method.invoke(target, (Object[]) null);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LifecycleElement)) {
                return false;
            }
            LifecycleElement otherElement = (LifecycleElement) other;
            return (this.identifier.equals(otherElement.identifier));
        }

        @Override
        public int hashCode() {
            return this.identifier.hashCode();
        }
    }

}
