package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.Result;
import com.ecommerce.order.common.enums.OrderStatus;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单控制器 - 提供订单CRUD及高级查询接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /api/orders
     */
    @PostMapping
    public Result createOrder(@RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        boolean success = orderService.save(order);
        if (success) {
            return Result.ok("订单创建成功").data(order);
        }
        return Result.fail("订单创建失败");
    }

    /**
     * 根据ID查询订单
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public Result getOrderById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order != null) {
            return Result.ok(order);
        }
        return Result.fail("订单不存在");
    }

    /**
     * 多条件分页查询订单列表
     * GET /api/orders?userId=1&status=0&keyword=手机&startDate=2024-01-01&endDate=2024-12-31&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result getOrderList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 按用户ID筛选
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        // 按状态筛选
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        // 关键词搜索（订单号/收货人/电话）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Order::getOrderNo, keyword)
                    .or()
                    .like(Order::getReceiverName, keyword)
                    .or()
                    .like(Order::getReceiverPhone, keyword)
            );
        }
        // 日期范围筛选
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Order::getCreatedAt, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Order::getCreatedAt, endDate + " 23:59:59");
        }

        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> resultPage = orderService.page(page, wrapper);

        return Result.ok(resultPage.getRecords())
                .total(resultPage.getTotal())
                .pageNum(resultPage.getCurrent())
                .pageSize(resultPage.getSize())
                .pages(resultPage.getPages());
    }

    /**
     * 更新订单
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    public Result updateOrder(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        order.setUpdatedAt(LocalDateTime.now());
        boolean success = orderService.updateById(order);
        if (success) {
            return Result.ok("订单更新成功").data(order);
        }
        return Result.fail("订单更新失败");
    }

    /**
     * 删除订单（逻辑删除）
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public Result deleteOrder(@PathVariable Long id) {
        boolean success = orderService.removeById(id);
        if (success) {
            return Result.ok("订单删除成功");
        }
        return Result.fail("订单删除失败");
    }

    /**
     * 根据订单编号查询订单
     * GET /api/orders/by-order-no/{orderNo}
     */
    @GetMapping("/by-order-no/{orderNo}")
    public Result getOrderByOrderNo(@PathVariable String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderService.getOne(wrapper);
        if (order != null) {
            return Result.ok(order);
        }
        return Result.fail("订单不存在");
    }

    /**
     * 批量删除订单
     * DELETE /api/orders/batch
     */
    @DeleteMapping("/batch")
    public Result batchDeleteOrders(@RequestBody List<Long> ids) {
        boolean success = orderService.removeByIds(ids);
        if (success) {
            return Result.ok("批量删除成功");
        }
        return Result.fail("批量删除失败");
    }

    /**
     * 订单统计概览
     * GET /api/orders/stats/overview
     */
    @GetMapping("/stats/overview")
    public Result getOrderStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        stats.put("totalOrders", orderService.count());

        // 各状态订单数
        for (OrderStatus s : OrderStatus.values()) {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getStatus, s.getCode());
            stats.put("status_" + s.getCode(), orderService.count(wrapper));
            stats.put("status_" + s.getCode() + "_desc", s.getDesc());
        }

        // 今日订单
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.apply("DATE(created_at) = CURDATE()");
        stats.put("todayOrders", orderService.count(todayWrapper));

        return Result.ok(stats);
    }
}
