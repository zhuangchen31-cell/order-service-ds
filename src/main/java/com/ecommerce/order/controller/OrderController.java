package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.Result;
import com.ecommerce.order.common.enums.OrderStatus;
import com.ecommerce.order.common.exception.BusinessException;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result createOrder(@RequestBody Order order) {
        if (order.getUserId() == null || order.getUserId() <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (order.getTotalAmount() == null) {
            throw new BusinessException("订单金额不能为空");
        }
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(order.getStatus() == null ? 0 : order.getStatus());
        order.setDeleted(0);
        if (!StringUtils.hasText(order.getOrderNo())) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        orderService.save(order);
        return Result.ok("订单创建成功").data(order);
    }

    @GetMapping("/{id}")
    public Result getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return Result.ok(order);
    }

    @GetMapping
    public Result getOrderList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Order> page = new Page<>(Math.max(pageNum, 1), Math.max(pageSize, 1));
        LambdaQueryWrapper<Order> wrapper = buildOrderWrapper(userId, status, keyword, startDate, endDate);
        return Result.page(orderService.page(page, wrapper));
    }

    @PutMapping("/{id}")
    public Result updateOrder(@PathVariable Long id, @RequestBody Order order) {
        if (orderService.getById(id) == null) {
            throw new BusinessException("订单不存在");
        }
        order.setId(id);
        order.setUpdatedAt(LocalDateTime.now());
        orderService.updateById(order);
        return Result.ok("订单更新成功").data(order);
    }

    @DeleteMapping("/{id}")
    public Result deleteOrder(@PathVariable Long id) {
        if (!orderService.removeById(id)) {
            throw new BusinessException("订单不存在或已删除");
        }
        return Result.ok("订单删除成功");
    }

    @GetMapping("/by-order-no/{orderNo}")
    public Result getOrderByOrderNo(@PathVariable String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderService.getOne(wrapper);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return Result.ok(order);
    }

    @DeleteMapping("/batch")
    public Result batchDeleteOrders(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的订单");
        }
        orderService.removeByIds(ids);
        return Result.ok("批量删除成功");
    }

    @GetMapping("/stats/overview")
    public Result getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderService.count());
        for (OrderStatus s : OrderStatus.values()) {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, s.getCode());
            stats.put("status_" + s.getCode(), orderService.count(wrapper));
            stats.put("status_" + s.getCode() + "_desc", s.getDesc());
        }
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Order::getCreatedAt, start);
        stats.put("todayOrders", orderService.count(todayWrapper));
        return Result.ok(stats);
    }

    private LambdaQueryWrapper<Order> buildOrderWrapper(Long userId, Integer status, String keyword, String startDate, String endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getReceiverName, keyword)
                    .or().like(Order::getReceiverPhone, keyword));
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Order::getCreatedAt, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Order::getCreatedAt, endDate + " 23:59:59");
        }
        return wrapper.orderByDesc(Order::getCreatedAt);
    }
}
