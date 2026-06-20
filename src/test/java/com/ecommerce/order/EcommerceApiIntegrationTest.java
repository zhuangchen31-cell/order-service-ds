package com.ecommerce.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EcommerceApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginProductsOrdersAndErrorsAreCovered() throws Exception {
        String token = loginToken();

        mockMvc.perform(get("/api/products/query")
                        .header("Authorization", bearer(token))
                        .param("pageNum", "1")
                        .param("pageSize", "2")
                        .param("category", "数码"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.page.pageNum").value(1))
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(1)));

        String productJson = """
                {
                  "name":"接口测试商品",
                  "category":"测试",
                  "description":"MockMvc 覆盖新增、修改、删除接口",
                  "price":88.80,
                  "stock":20,
                  "status":1
                }
                """;
        String addResponse = mockMvc.perform(post("/api/products/add")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品新增成功"))
                .andReturn().getResponse().getContentAsString();
        long productId = objectMapper.readTree(addResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/products/update/{id}", productId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson.replace("88.80", "99.90")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(99.90));

        mockMvc.perform(delete("/api/products/delete/{id}", productId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品删除成功"));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId":2,
                                  "totalAmount":188.60,
                                  "status":1,
                                  "receiverName":"测试用户",
                                  "receiverPhone":"13800138000",
                                  "shippingAddress":"接口测试地址"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("订单创建成功"))
                .andExpect(jsonPath("$.data.orderNo").exists());

        mockMvc.perform(post("/api/products/add")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"category\":\"测试\",\"price\":0,\"stock\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/not-exists")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("接口不存在")));
    }

    private String loginToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenInfo.token").exists())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("tokenInfo").path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
