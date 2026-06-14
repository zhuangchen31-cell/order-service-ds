package com.ecommerce.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.order.entity.User;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 注册用户（密码BCrypt加密）
     */
    User register(String username, String password, String phone, String email);

    /**
     * 验证用户名密码，返回用户
     */
    User login(String username, String password);
}
