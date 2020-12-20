package pers.amos.ioc;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pers.amos.ioc.service.HelloService;

/**
 * @author amos wong
 * @create 2020-12-20 9:02 上午
 */

public class ContainerStartTest {

    public static void main(String[] args) {
        // 我们平常使用AnnotationConfigApplicationContext的时候，只需要这样直接new出来就好了
        ApplicationContext context = new AnnotationConfigApplicationContext("pers.amos.ioc");

        // 然后就可以从容器中拿到bean对象了，说明其实new创建对象的时候，我们容器就做好启动初始化工作了~
        HelloService service = context.getBean(HelloService.class);
        service.sayHello();
    }
}
