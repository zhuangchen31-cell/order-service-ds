package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.Result;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单明细控制器 - 提供订单商品明细CRUD接口
 */
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /**
     * 创建订单明细
     * POST /api/order-items
     */
    @PostMapping
    public Result createOrderItem(@RequestBody OrderItem orderItem) {
        boolean success = orderItemService.save(orderItem);
        if (success) {
            return Result.ok("订单明细创建成功").data(orderItem);
        }
        return Result.fail("订单明细创建失败");
    }

    /**
     * 根据ID查询订单明细
     * GET /api/order-items/{id}
     */
    @GetMapping("/{id}")
    public Result getOrderItemById(@PathVariable Long id) {
        OrderItem item = orderItemService.getById(id);
        if (item != null) {
            return Result.ok(item);
        }
        return Result.fail("订单明细不存在");
    }

    /**
     * 根据订单ID查询明细列表
     * GET /api/order-items/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Result getItemsByOrderId(@PathVariable Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(wrapper);
        return Result.ok(items);
    }

    /**
     * 分页查询订单明细列表
     * GET /api/order-items?pageNum=1&pageSize=10
     */
    @GetMapping
    public Result getOrderItemList(
            @RequestParam(required = false) Long orderId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<OrderItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(OrderItem::getOrderId, orderId);
        }
        wrapper.orderByDesc(OrderItem::getCreatedAt);

        IPage<OrderItem> resultPage = orderItemService.page(page, wrapper);

        return Result.ok(resultPage.getRecords())
                .total(resultPage.getTotal())
                .pageNum(resultPage.getCurrent())
                .pageSize(resultPage.getSize())
                .pages(resultPage.getPages());
    }

    /**
     * 更新订单明细
     * PUT /api/order-items/{id}
     */
    @PutMapping("/{id}")
    public Result updateOrderItem(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        orderItem.setId(id);
        boolean success = orderItemService.updateById(orderItem);
        if (success) {
            return Result.ok("订单明细更新成功").data(orderItem);
        }
        return Result.fail("订单明细更新失败");
    }

    /**
     * 删除订单明细
     * DELETE /api/order-items/{id}
     */
    @DeleteMapping("/{id}")
    public Result deleteOrderItem(@PathVariable Long id) {
        boolean success = orderItemService.removeById(id);
        if (success) {
            return Result.ok("订单明细删除成功");
        }
        return Result.fail("订单明细删除失败");
    }

    /**
     * 批量删除订单明细
     * DELETE /api/order-items/batch
     */
    @DeleteMapping("/batch")
    public Result batchDeleteOrderItems(@RequestBody List<Long> ids) {
        boolean success = orderItemService.removeByIds(ids);
        if (success) {
            return Result.ok("批量删除成功");
        }
        return Result.fail("批量删除失败");
    }
}
