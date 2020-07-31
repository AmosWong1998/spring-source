package com.imooc.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope(value = "prototype")
public class Company {
    private Staff staff;

    // 构造器方式注入
    @Autowired
    public Company(Staff staff) {
        this.staff = staff;
    }
}
