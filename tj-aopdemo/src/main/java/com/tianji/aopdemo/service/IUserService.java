package com.tianji.aopdemo.service;

import com.tianji.aopdemo.domain.User;

import java.util.List;

public interface IUserService {
    User getUserById(Long id);

    List<User> list();
}
