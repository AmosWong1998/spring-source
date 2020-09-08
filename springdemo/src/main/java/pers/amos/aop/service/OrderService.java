package pers.amos.aop.service;


import pers.amos.aop.domain.Order;

public interface OrderService {

    Order createOrder(String username, String product);

    Order queryOrder(String username);
}