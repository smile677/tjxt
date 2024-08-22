package com.tianji.aopdemo.controller;

import com.tianji.aopdemo.domain.User;
import com.tianji.aopdemo.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @RequestMapping("/{id}")
    public User getUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    @RequestMapping("/list")
    public List<User> list() {
        return userService.list();
    }
}
