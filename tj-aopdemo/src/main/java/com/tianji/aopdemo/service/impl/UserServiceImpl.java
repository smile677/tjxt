package com.tianji.aopdemo.service.impl;

import com.tianji.aopdemo.config.PrintTime;
import com.tianji.aopdemo.domain.User;
import com.tianji.aopdemo.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements IUserService {
    @Override
    @PrintTime(title = "用户模块")
    public User getUserById(Long id) {
        log.debug("方法运行");
        User user = new User(id, "张三", 18);
        return null;
    }


    @Override
//    @PrintTime(title = "分页模块")
    public List<User> list() {
        User u1 = new User(1L, "张三1", 18);
        User u2 = new User(2L, "张三2", 18);
        User u3 = new User(3L, "张三3", 18);
        List<User> list = new ArrayList<>();
        list.add(u1);
        list.add(u2);
        list.add(u3);
        return list;
    }
}
