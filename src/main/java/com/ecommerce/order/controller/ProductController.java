package com.ecommerce.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.order.common.Result;
import com.ecommerce.order.common.exception.BusinessException;
import com.ecommerce.order.entity.Product;
import com.ecommerce.order.service.ProductService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping({"", "/add"})
    public Result add(@Valid @RequestBody Product product) {
        product.setId(null);
        product.setStatus(product.getStatus() == null ? 1 : product.getStatus());
        product.setDeleted(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productService.save(product);
        return Result.ok("商品新增成功").data(product);
    }

    @DeleteMapping({"/{id}", "/delete/{id}"})
    public Result delete(@PathVariable Long id) {
        if (!productService.removeById(id)) {
            throw new BusinessException("商品不存在或已删除");
        }
        return Result.ok("商品删除成功");
    }

    @PutMapping({"/{id}", "/update/{id}"})
    public Result update(@PathVariable Long id, @Valid @RequestBody Product product) {
        if (productService.getById(id) == null) {
            throw new BusinessException("商品不存在");
        }
        product.setId(id);
        product.setUpdatedAt(LocalDateTime.now());
        productService.updateById(product);
        return Result.ok("商品更新成功").data(product);
    }

    @GetMapping({"", "/query"})
    public Result query(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize) {
        Page<Product> page = new Page<>(Math.max(pageNum, 1), Math.max(pageSize, 1));
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        IPage<Product> result = productService.page(page, wrapper);
        return Result.page(result);
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return Result.ok(product);
    }
}
