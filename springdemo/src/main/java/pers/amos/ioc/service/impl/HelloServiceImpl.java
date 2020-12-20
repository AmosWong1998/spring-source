package pers.amos.ioc.service.impl;

import org.springframework.stereotype.Service;
import pers.amos.ioc.service.HelloService;

/**
 * @author amos wong
 * @create 2020-12-20 9:04 上午
 */
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public void sayHello() {
        System.out.println("hello amos!");
    }
}
