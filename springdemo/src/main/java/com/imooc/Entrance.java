package com.imooc;

import com.imooc.aspect.OutSide;
import com.imooc.controller.HiController;
import com.imooc.controller.WelcomeController;
import com.imooc.entity.User;
import com.imooc.introduction.LittleUniverse;
import com.imooc.service.HelloService;
import com.imooc.service.HiService;
import com.imooc.service.WelcomeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.FileSystemXmlApplicationContext;

@Configuration
@EnableAspectJAutoProxy
@Import(OutSide.class)
@ComponentScan("com.imooc")
//@ComponentScan(basePackages = {"com.imooc.service", "com.imooc.postprocessor", "com.imooc.entity"})
public class Entrance {

    public static void main1(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Entrance.class);
        System.out.println("Hello 我正在学习Spring Framework");
        System.out.println("------------------------------");
        HiService hiService = (HiService) applicationContext.getBean("hiServiceImpl");
        hiService.sayHi();
        System.out.println("---------------------------分割线以下执行HelloService-------------------------------");
        HelloService helloService = (HelloService) applicationContext.getBean("helloServiceImpl");
        helloService.sayHello();
        OutSide outSide = (OutSide) applicationContext.getBean("com.imooc.aspect.OutSide");
        outSide.say();


    }

    public static void main0(String[] args) {
        ApplicationContext annotationApplicationContext = new AnnotationConfigApplicationContext(Entrance.class);
        System.out.println("AOP--------------------------------------");
        HiService hiService = (HiService) annotationApplicationContext.getBean("hiServiceImpl");
        hiService.sayHi();
        System.out.println("-------------以下开始执行HelloService----------------------");
        HelloService helloService = (HelloService) annotationApplicationContext.getBean("helloServiceImpl");
        helloService.sayHello();
    }


    public static void main3(String[] args) {

        ApplicationContext annotationApplicationContext = new AnnotationConfigApplicationContext(Entrance.class);
        System.out.println("AOP--------------------------------------");
//        WelcomeService welcomeService2 = (WelcomeService) annotationApplicationContext.getBean("welcomeServiceImpl");
//        welcomeService2.sayHello("强大的Spring框架");
//        User user5 = (User) annotationApplicationContext.getBean("user5");
//        System.out.println("CustomizedBeanDefinitionRegistryPostProcessor创建的对象 ; " + user5);
//        HelloController helloController = (HelloController) annotationApplicationContext.getBean("helloController");
//        helloController.handleRequest();
//        HiController hiController = (HiController) annotationApplicationContext.getBean("hiController");
//        hiController.handleRequest();
        HiController hiController = (HiController) annotationApplicationContext.getBean("hiController");
        ((LittleUniverse) hiController).burningup();

    }

    public static void main2(String[] args) {
        System.out.println("Hello World!");
        String xmlPath = "D:\\LearningMaterials\\SpringFramework\\spring\\springdemo\\src\\main\\resources\\spring\\spring-config.xml";
        FileSystemXmlApplicationContext applicationContext = new FileSystemXmlApplicationContext(xmlPath);
        WelcomeService welcomeService = (WelcomeService) applicationContext.getBean("welcomeService");
        welcomeService.sayHello("强大的Spring框架");
        System.out.println("-----------------------------------");

    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Entrance.class);
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
        }
        WelcomeController welcomeController = (WelcomeController) applicationContext.getBean("welcomeController");
        welcomeController.handleRequest();
        User user5 = (User) applicationContext.getBean("user5");
        System.out.println("创建的对象：" + user5);
    }

}
