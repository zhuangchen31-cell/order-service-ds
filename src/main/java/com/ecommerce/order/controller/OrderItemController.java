package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单明细控制器
 */
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderItem>> createOrderItem(@RequestBody OrderItem orderItem) {
        boolean success = orderItemService.save(orderItem);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单明细创建成功", orderItem));
        }
        return ResponseEntity.ok(ApiResponse.error("订单明细创建失败"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderItem>> getOrderItemById(@PathVariable Long id) {
        OrderItem item = orderItemService.getById(id);
        if (item != null) {
            return ResponseEntity.ok(ApiResponse.success(item));
        }
        return ResponseEntity.ok(ApiResponse.error("订单明细不存在"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getItemsByOrderId(@PathVariable Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(wrapper);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<IPage<OrderItem>>> getOrderItemList(
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
        return ResponseEntity.ok(ApiResponse.success(resultPage));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderItem>> updateOrderItem(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        orderItem.setId(id);
        boolean success = orderItemService.updateById(orderItem);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单明细更新成功", orderItem));
        }
        return ResponseEntity.ok(ApiResponse.error("订单明细更新失败"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrderItem(@PathVariable Long id) {
        boolean success = orderItemService.removeById(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("订单明细删除成功", null));
        }
        return ResponseEntity.ok(ApiResponse.error("订单明细删除失败"));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteOrderItems(@RequestBody List<Long> ids) {
        boolean success = orderItemService.removeByIds(ids);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
        }
        return ResponseEntity.ok(ApiResponse.error("批量删除失败"));
    }
}
