package com.ecommerce.order.controller;

import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.dto.LoginRequest;
import com.ecommerce.order.dto.LoginResponse;
import com.ecommerce.order.entity.User;
import com.ecommerce.order.service.UserService;
import com.ecommerce.order.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        if (!userService.authenticate(request.getUsername(), request.getPassword())) {
            return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
        }

        User user = userService.findByUsername(request.getUsername());
        String token = jwtTokenUtil.generateToken(user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(jwtTokenUtil.getExpirationInSeconds());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        response.setUser(userInfo);

        return ResponseEntity.ok(ApiResponse.success("登录成功", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        User registered = userService.register(user);
        if (registered == null) {
            return ResponseEntity.ok(ApiResponse.error("用户名已存在"));
        }
        registered.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success("注册成功", registered));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getUserInfo(@RequestAttribute("username") String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error("用户不存在"));
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
