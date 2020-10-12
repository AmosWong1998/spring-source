package pers.amos.aop.cyclic_dependence;

import java.util.HashMap;
import java.util.Map;

/**
 * @author amos wong
 * @create 2020-09-23 10:03 上午
 * <p>
 * 自己用伪代码 模拟解决循环依赖 思路是一致的
 */

public class AmosSolveCyclicDependence {

    /**
     * 存放已经初始化完成的bean
     */
    private Map<String, Object> singleMap = new HashMap<>(16);

    /**
     * 存放早期暴露，仅仅实例化，没有依赖注入的bean 用于解决循环依赖
     */
    private Map<String, Object> earlyReference = new HashMap<>(16);

    protected Object getBean(final String beanName) {
        // 看一下目标bean是否已经完全初始化了，如果完全初始化则返回
        Object single = singleMap.get(beanName);
        if (single != null) {
            return single;
        }
        // 看目标bean是否已经创建，如果创建了则直接返回
        single = earlyReference.get(beanName);
        if (single != null) {
            return single;
        }

        Object beanInstance = createBeanInstance(beanName);
        // 实例创建之后，放入缓存
        // 因为已经创建实例了，这个时候这个实例的引用暴露出去已经没问题了
        // 之后的属性注入等逻辑还是在这个实例上做的
        earlyReference.put(beanName, beanInstance);
        populateBean(beanName, beanInstance); // 属性注入
        initializeBean(beanName, beanInstance);// 初始化方法
        earlyReference.remove(beanName);
        singleMap.put(beanName, beanInstance);

        return beanInstance;
    }

    /**
     * 模拟创建bean实例的方法
     * @param beanName
     * @return
     */
    private Object createBeanInstance(String beanName) {
        return new Object();
    }

    /**
     * 模拟填充属性的方法
     * @param beanName
     * @param beanInstance
     */
    private void populateBean(String beanName, Object beanInstance) {
        System.out.println("填充属性的方法：beanName=" + beanName);
    }

    private void initializeBean(String beanName, Object beanInstance) {
        System.out.println("初始化方法，初始化：" + beanName);
    }
}
