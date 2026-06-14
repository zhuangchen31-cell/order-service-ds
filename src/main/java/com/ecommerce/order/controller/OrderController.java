package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.Result;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器 - 提供订单CRUD接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * POST /api/orders
     */
    @PostMapping
    public Result createOrder(@RequestBody Order order) {
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
     * 分页查询订单列表（支持按用户ID和状态筛选）
     * GET /api/orders?userId=1&status=0&pageNum=1&pageSize=10
     */
    @GetMapping
    public Result getOrderList(
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
}
