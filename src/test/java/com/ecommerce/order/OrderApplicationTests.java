package com.ecommerce.order;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.util.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private String getToken() {
        return jwtTokenUtil.generateToken("user1");
    }

    @Test
    public void testCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderNo\":\"TEST2024001\",\"userId\":2,\"totalAmount\":100.00,\"status\":0,\"receiverName\":\"测试\",\"receiverPhone\":\"13800000000\",\"shippingAddress\":\"测试地址\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetOrders() throws Exception {
        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer " + getToken())
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testGetOrderById() throws Exception {
        mockMvc.perform(get("/api/orders/1")
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testUpdateOrder() throws Exception {
        mockMvc.perform(put("/api/orders/1")
                .header("Authorization", "Bearer " + getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/999")
                .header("Authorization", "Bearer " + getToken()))
                .andExpect(status().isOk());
    }
}
