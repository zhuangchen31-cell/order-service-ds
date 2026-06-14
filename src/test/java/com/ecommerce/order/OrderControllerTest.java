package com.ecommerce.order;

import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.controller.OrderItemController;
import com.ecommerce.order.dto.ApiResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.service.OrderItemService;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Controller 层单元测试 - 使用 Mockito 模拟 Service 层
 */
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderController orderController;

    @InjectMocks
    private OrderItemController orderItemController;

    // ============ Order 订单测试 ============

    @Test
    public void testCreateOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD2024010001");
        order.setUserId(1L);
        order.setTotalAmount(new BigDecimal("299.99"));
        order.setStatus(0);

        when(orderService.save(any(Order.class))).thenReturn(true);

        ResponseEntity<ApiResponse<Order>> response = orderController.createOrder(order);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertEquals("订单创建成功", response.getBody().getMessage());
    }

    @Test
    public void testGetOrderById() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD2024010001");
        order.setUserId(1L);

        when(orderService.getById(1L)).thenReturn(order);

        ResponseEntity<ApiResponse<Order>> response = orderController.getOrderById(1L);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    public void testGetOrderByIdNotFound() {
        when(orderService.getById(999L)).thenReturn(null);

        ResponseEntity<ApiResponse<Order>> response = orderController.getOrderById(999L);
        assertNotNull(response);
        assertFalse(response.getBody().getSuccess());
        assertEquals("订单不存在", response.getBody().getMessage());
    }

    @Test
    public void testUpdateOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(2);

        when(orderService.updateById(any(Order.class))).thenReturn(true);

        ResponseEntity<ApiResponse<Order>> response = orderController.updateOrder(1L, order);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertEquals("订单更新成功", response.getBody().getMessage());
    }

    @Test
    public void testDeleteOrder() {
        when(orderService.removeById(1L)).thenReturn(true);

        ResponseEntity<ApiResponse<Void>> response = orderController.deleteOrder(1L);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertEquals("订单删除成功", response.getBody().getMessage());
    }

    @Test
    public void testGetOrderByOrderNo() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD2024010001");
        order.setUserId(1L);

        when(orderService.getOne(any())).thenReturn(order);

        ResponseEntity<ApiResponse<Order>> response = orderController.getOrderByOrderNo("ORD2024010001");
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
    }

    // ============ OrderItem 订单明细测试 ============

    @Test
    public void testCreateOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setProductId(1001L);
        item.setProductName("iPhone15 Pro");
        item.setPrice(new BigDecimal("7999.00"));
        item.setQuantity(1);
        item.setSubTotal(new BigDecimal("7999.00"));

        when(orderItemService.save(any(OrderItem.class))).thenReturn(true);

        ResponseEntity<ApiResponse<OrderItem>> response = orderItemController.createOrderItem(item);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertEquals("订单明细创建成功", response.getBody().getMessage());
    }

    @Test
    public void testGetOrderItemById() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrderId(1L);
        item.setProductName("iPhone15 Pro");

        when(orderItemService.getById(1L)).thenReturn(item);

        ResponseEntity<ApiResponse<OrderItem>> response = orderItemController.getOrderItemById(1L);
        assertNotNull(response);
        assertTrue(response.getBody().getSuccess());
        assertNotNull(response.getBody().getData());
    }

    @Test
    public void testGetOrderItemByIdNotFound() {
        when(orderItemService.getById(999L)).thenReturn(null);

        ResponseEntity<ApiResponse<OrderItem>> response = orderItemController.getOrderItemById(999L);
        assertNotNull(response);
        assertFalse(response.getBody().getSuccess());
        assertEquals("订单明细不存在", response.getBody().getMessage());
    }
}
