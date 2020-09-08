package pers.amos.aop.service;


import pers.amos.aop.domain.User;

/**
 * @author amos wong
 * @create 2020-09-03 11:23 上午
 */

public interface UserService {

    User createUser(String firstName, String lastName, int age);

    User queryUser();
}
