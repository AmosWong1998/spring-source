package com.newstart.lifecycle;

import org.springframework.stereotype.Component;

/**
 * @author amos wong
 * @create 2020-10-12 8:05 上午
 */
@Component
public class MyAnnoClass {

    public void sayHello() {
        System.out.println("hello I am annotation class!");
    }
}
