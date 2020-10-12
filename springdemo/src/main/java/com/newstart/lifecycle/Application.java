package com.newstart.lifecycle;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author amos wong
 * @create 2020-10-12 8:06 上午
 */

public class Application {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.newstart.lifecycle");
        MyAnnoClass annoClass = context.getBean(MyAnnoClass.class);
        annoClass.sayHello();
    }
}
