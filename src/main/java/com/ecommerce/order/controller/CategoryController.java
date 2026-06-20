package com.ecommerce.order.controller;

import com.ecommerce.order.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final List<Map<String, Object>> CATEGORIES = Arrays.asList(
            category(1, "电子产品", "📱"),
            category(2, "服装鞋帽", "👕"),
            category(3, "家居用品", "🏠"),
            category(4, "食品饮料", "🍎"),
            category(5, "图书文具", "📚"),
            category(6, "运动户外", "⚽")
    );

    private static Map<String, Object> category(int id, String name, String icon) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("icon", icon);
        return map;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        return ResponseEntity.ok(ApiResponse.success(CATEGORIES));
    }
}
