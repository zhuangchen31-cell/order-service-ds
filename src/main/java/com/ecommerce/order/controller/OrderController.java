package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        boolean success = orderService.save(order);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单创建成功", order));
        }
        return ResponseEntity.ok(ApiResponse.error("订单创建失败"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order != null) {
            return ResponseEntity.ok(ApiResponse.success(order));
        }
        return ResponseEntity.ok(ApiResponse.error("订单不存在"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.eq(Order::getDeleted, 0);
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> resultPage = orderService.page(page, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("pageNum", resultPage.getCurrent());
        result.put("pageSize", resultPage.getSize());
        result.put("pages", resultPage.getPages());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        boolean success = orderService.updateById(order);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单更新成功", order));
        }
        return ResponseEntity.ok(ApiResponse.error("订单更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        boolean success = orderService.removeById(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单删除成功", null));
        }
        return ResponseEntity.ok(ApiResponse.error("订单删除失败"));
    }

    @GetMapping("/by-order-no/{orderNo}")
    public ResponseEntity<ApiResponse<Order>> getOrderByOrderNo(@PathVariable String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getDeleted, 0);
        Order order = orderService.getOne(wrapper);

        if (order != null) {
            return ResponseEntity.ok(ApiResponse.success(order));
        }
        return ResponseEntity.ok(ApiResponse.error("订单不存在"));
    }
}
