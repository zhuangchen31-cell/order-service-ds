package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private String username;
    private String role;
    private Long userId;

    public static LoginResponse of(String token, String username, String role, Long userId) {
        return new LoginResponse(token, "Bearer", username, role, userId);
    }
}
