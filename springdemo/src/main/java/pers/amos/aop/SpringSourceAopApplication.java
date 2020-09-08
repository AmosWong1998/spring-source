package pers.amos.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import pers.amos.aop.service.OrderService;
import pers.amos.aop.service.UserService;

/**
 * @author amos wong
 * @create 2020-09-08 3:38 下午
 */

public class SpringSourceAopApplication {

    public static void main(String[] args) {
        // 启动 Spring 的 IOC 容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/spring/spring-aop.xml");

        UserService userService = context.getBean(UserService.class);
        OrderService orderService = context.getBean(OrderService.class);

        userService.createUser("Tom", "Cruise", 55);
        userService.queryUser();

        orderService.createOrder("Leo", "随便买点什么");
        orderService.queryOrder("Leo");
    }
}
