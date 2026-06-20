package com.ecommerce.order.controller;

import com.ecommerce.order.common.Result;
import com.ecommerce.order.entity.User;
import com.ecommerce.order.service.UserService;
import com.ecommerce.order.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return Result.fail("用户名或密码不能为空");
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        if (user.getEnabled() == 0) {
            return Result.fail("账号已被禁用");
        }

        // Simple password check (BCrypt in real app)
        if (!userService.authenticate(username, password)) {
            return Result.fail("用户名或密码错误");
        }

        String token = jwtTokenUtil.generateToken(username);

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", token);
        tokenInfo.put("tokenType", "Bearer");
        tokenInfo.put("username", username);
        tokenInfo.put("expiresIn", jwtTokenUtil.getExpirationInSeconds());

        Map<String, Object> data = new HashMap<>();
        data.put("tokenInfo", tokenInfo);
        data.put("user", user);

        return Result.ok("登录成功").data(data);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String phone = request.get("phone");
        String email = request.get("email");

        if (username == null || username.isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            return Result.fail("密码至少6位");
        }

        User exist = userService.findByUsername(username);
        if (exist != null) {
            return Result.fail("用户名已存在");
        }

        User user = userService.register(username, password, phone, email);
        user.setPassword(null);

        String token = jwtTokenUtil.generateToken(username);

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", token);
        tokenInfo.put("tokenType", "Bearer");
        tokenInfo.put("username", username);

        Map<String, Object> data = new HashMap<>();
        data.put("tokenInfo", tokenInfo);
        data.put("user", user);

        return Result.ok("注册成功").data(data);
    }
}
