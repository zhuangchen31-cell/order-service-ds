package com.ecommerce.order.controller;

import com.ecommerce.order.common.Result;
import com.ecommerce.order.dto.LoginRequest;
import com.ecommerce.order.dto.LoginResponse;
import com.ecommerce.order.dto.RegisterRequest;
import com.ecommerce.order.entity.User;
import com.ecommerce.order.security.JwtTokenProvider;
import com.ecommerce.order.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理登录和注册
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getPhone(),
                request.getEmail()
        );

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("tokenInfo", LoginResponse.of(token, user.getUsername(), user.getRole(), user.getId()));

        return Result.ok("注册成功").data(data);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse loginResponse = LoginResponse.of(
                token, user.getUsername(), user.getRole(), user.getId()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("tokenInfo", loginResponse);
        data.put("user", user);

        return Result.ok("登录成功").data(data);
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Result getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userService.findByUsername(username);

        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setPassword(null); // 不返回密码
        return Result.ok(user);
    }
}
