package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.order.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    boolean authenticate(String username, String password);
    User register(User user);
}
