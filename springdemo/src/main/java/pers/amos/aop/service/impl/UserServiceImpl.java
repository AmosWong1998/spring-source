package pers.amos.aop.service.impl;

import pers.amos.aop.domain.User;
import pers.amos.aop.service.UserService;

/**
 * @author amos wong
 * @create 2020-09-03 11:24 上午
 */

public class UserServiceImpl implements UserService {

    @Override
    public User createUser(String firstName, String lastName, int age) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        return user;
    }

    @Override
    public User queryUser() {
        User user = new User();
        user.setFirstName("test");
        user.setLastName("test");
        user.setAge(20);
        return user;
    }
}
