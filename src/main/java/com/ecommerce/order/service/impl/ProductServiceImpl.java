package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.order.entity.Product;
import com.ecommerce.order.mapper.ProductMapper;
import com.ecommerce.order.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * 商品Service实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {
}
