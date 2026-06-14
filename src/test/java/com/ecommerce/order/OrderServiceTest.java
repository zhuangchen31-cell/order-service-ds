package com.ecommerce.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.service.OrderItemService;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service 层集成测试 - 使用 H2 内存数据库
 * 测试 MyBatis Plus CRUD 操作
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    private static Long createdOrderId;

    // ============ 订单 CRUD 测试 ============

    @Test
    @org.junit.jupiter.api.Order(1)
    public void test01CreateOrder() {
        Order order = new Order();
        order.setOrderNo("TEST" + System.currentTimeMillis());
        order.setUserId(100L);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setStatus(0);
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13900000000");
        order.setShippingAddress("测试地址");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        boolean result = orderService.save(order);
        assertTrue(result);
        assertNotNull(order.getId());
        createdOrderId = order.getId();
        System.out.println("✓ 创建订单成功, ID=" + createdOrderId);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    public void test02GetOrderById() {
        // 先创建再查询
        Order order = orderService.getById(createdOrderId);
        assertNotNull(order);
        assertNotNull(order.getOrderNo());
        assertEquals(new BigDecimal("500.00"), order.getTotalAmount());
        System.out.println("✓ 查询订单: " + order.getOrderNo() + ", 金额: " + order.getTotalAmount());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    public void test03ListOrders() {
        List<Order> orders = orderService.list();
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
        System.out.println("✓ 订单总数: " + orders.size());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    public void test04UpdateOrder() {
        Order order = orderService.getById(createdOrderId);
        assertNotNull(order);
        order.setStatus(1);  // 改为已支付
        order.setUpdatedAt(LocalDateTime.now());
        boolean result = orderService.updateById(order);
        assertTrue(result);

        Order updated = orderService.getById(createdOrderId);
        assertEquals(1, updated.getStatus());
        System.out.println("✓ 更新订单状态成功: " + updated.getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    public void test05PageQuery() {
        Page<Order> page = new Page<>(1, 10);
        IPage<Order> result = orderService.page(page);
        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
        System.out.println("✓ 分页查询: 总数=" + result.getTotal() + ", 当前页=" + result.getCurrent());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    public void test06QueryByCondition() {
        // 按用户ID和状态查询
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, 100L);

        List<Order> orders = orderService.list(wrapper);
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
        System.out.println("✓ 按用户ID查询: " + orders.size() + " 条");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    public void test07DeleteOrder() {
        boolean result = orderService.removeById(createdOrderId);
        assertTrue(result);

        // 逻辑删除后查不到
        Order deleted = orderService.getById(createdOrderId);
        assertNull(deleted);
        System.out.println("✓ 逻辑删除订单成功");
    }

    // ============ 订单明细 CRUD 测试 ============

    @Test
    @org.junit.jupiter.api.Order(8)
    public void test08CreateOrderItem() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        item.setProductId(2001L);
        item.setProductName("测试商品");
        item.setSkuId(3001L);
        item.setSkuCode("SKU-TEST-001");
        item.setPrice(new BigDecimal("99.99"));
        item.setQuantity(2);
        item.setSubTotal(new BigDecimal("199.98"));
        item.setCreatedAt(LocalDateTime.now());

        boolean result = orderItemService.save(item);
        assertTrue(result);
        assertNotNull(item.getId());
        System.out.println("✓ 创建订单明细成功, ID=" + item.getId());
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    public void test09ListOrderItems() {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, 1L);

        List<OrderItem> items = orderItemService.list(wrapper);
        assertNotNull(items);
        System.out.println("✓ 查询订单明细: " + items.size() + " 条");
    }
}
